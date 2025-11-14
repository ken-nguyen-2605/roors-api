package com.josephken.roors.order.service;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.menu.entity.MenuItem;
import com.josephken.roors.menu.repository.MenuItemRepository;
import com.josephken.roors.order.dto.*;
import com.josephken.roors.order.entity.Order;
import com.josephken.roors.order.entity.OrderItem;
import com.josephken.roors.order.entity.OrderStatus;
import com.josephken.roors.order.repository.OrderRepository;
import com.josephken.roors.payment.dto.PaymentResponse;
import com.josephken.roors.payment.entity.Payment;
import com.josephken.roors.payment.service.PaymentService;
import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import jakarta.persistence.EntityNotFoundException;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse createOrder(User user, CreateOrderRequest request) {
        log.info(LogCategory.order("Creating order for user: " + user.getEmail()));

        // Validate menu items and calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        
        Order order = new Order();
        order.setUser(user);
        order.setOrderType(request.getOrderType());
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : user.getUsername());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : user.getEmail());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setTableNumber(request.getTableNumber());

        // Add order items
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemRequest.getMenuItemId()));

            if (!menuItem.getIsAvailable()) {
                throw new RuntimeException("Menu item is not available: " + menuItem.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            
            BigDecimal itemSubtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(itemSubtotal);
            subtotal = subtotal.add(itemSubtotal);

            order.addOrderItem(orderItem);
        }

        // Calculate totals
        order.setSubtotal(subtotal);
        order.setTaxAmount(subtotal.multiply(BigDecimal.valueOf(0.10))); // 10% tax
        
        // Delivery fee (if delivery type)
        if (request.getOrderType().name().equals("DELIVERY")) {
            order.setDeliveryFee(BigDecimal.valueOf(20000)); // 20,000 VND
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }
        
        order.setDiscountAmount(BigDecimal.ZERO);
        
        BigDecimal total = subtotal
                .add(order.getTaxAmount())
                .add(order.getDeliveryFee())
                .subtract(order.getDiscountAmount());
        order.setTotalAmount(total);

        // Estimate preparation time
        int totalPrepTime = order.getOrderItems().stream()
                .mapToInt(item -> {
                    Integer prepTime = item.getMenuItem().getPreparationTime();
                    return prepTime != null ? prepTime : 15;
                })
                .max()
                .orElse(30);
        order.setEstimatedPreparationTime(totalPrepTime);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create payment
        Payment payment = paymentService.createPayment(savedOrder, request.getPaymentMethod());

        log.info(LogCategory.order("Order created successfully: " + savedOrder.getOrderNumber()));

        return mapToResponse(savedOrder, payment);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(User user, int page, int size, OrderStatus status) {
        log.info(LogCategory.order("Fetching orders for user: " + user.getEmail()));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByUserAndStatus(user, status, pageable);
        } else {
            orders = orderRepository.findByUser(user, pageable);
            log.info(LogCategory.order("User's id {}" + user.getId()));
        }

        log.info(LogCategory.order("Fetched " + orders.getTotalElements() + " orders for user: " + user.getEmail()));
            
        
        return orders.map(order -> {
            try {
                PaymentResponse payment = paymentService.getPaymentByOrder(order);
                return mapToResponseWithPayment(order, payment);
            } catch (Exception e) {
                return mapToResponseWithPayment(order, null);
            }
        });
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(User user, Long orderId) {
        log.info(LogCategory.order("Fetching order: " + orderId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        try {
            PaymentResponse payment = paymentService.getPaymentByOrder(order);
            return mapToResponseWithPayment(order, payment);
        } catch (Exception e) {
            return mapToResponseWithPayment(order, null);
        }
    }

    @Transactional
    public OrderResponse updateOrder(User user, Long orderId, UpdateOrderRequest request) {
        log.info(LogCategory.order("Updating order: " + orderId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        // Check if order can be modified
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be modified in current status: " + order.getStatus());
        }

        // Clear existing items
        order.getOrderItems().clear();

        // Add new items and recalculate
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemRequest.getMenuItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            
            BigDecimal itemSubtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(itemSubtotal);
            subtotal = subtotal.add(itemSubtotal);

            order.addOrderItem(orderItem);
        }

        // Update totals
        order.setSubtotal(subtotal);
        order.setTaxAmount(subtotal.multiply(BigDecimal.valueOf(0.10)));
        
        BigDecimal total = subtotal
                .add(order.getTaxAmount())
                .add(order.getDeliveryFee())
                .subtract(order.getDiscountAmount());
        order.setTotalAmount(total);

        // Update other fields
        if (request.getSpecialInstructions() != null) {
            order.setSpecialInstructions(request.getSpecialInstructions());
        }
        if (request.getDeliveryAddress() != null) {
            order.setDeliveryAddress(request.getDeliveryAddress());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info(LogCategory.order("Order updated successfully: " + orderId));

        return mapToResponse(updatedOrder, null);
    }

    @Transactional
    public OrderResponse cancelOrder(User user, Long orderId, CancelOrderRequest request) {
        log.info(LogCategory.order("Cancelling order: " + orderId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Completed orders cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.PREPARING || 
            order.getStatus() == OrderStatus.READY ||
            order.getStatus() == OrderStatus.DELIVERING) {
            throw new RuntimeException("Order is already being prepared and cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(request.getReason());

        Order cancelledOrder = orderRepository.save(order);
        log.info(LogCategory.order("Order cancelled successfully: " + orderId));

        return mapToResponse(cancelledOrder, null);
    }

    public Page<OrderResponse> getOrdersByDate(LocalDate date, int page, int size, OrderStatus status) {
        // Create Pageable object (Sort by newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Calculate Start of Day (e.g., 2025-11-13 00:00:00)
        LocalDateTime startOfDay = date.atStartOfDay();

        // Calculate End of Day (e.g., 2025-11-13 23:59:59.999999)
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Page<Order> orders;

        // Check if Status is provided or null
        // if (status != null) {
        //     // Filter by Date Range AND Status
        //     //orders = orderRepository.findByCreatedAtBetweenAndStatus(startOfDay, endOfDay, status, pageable);
        // } else {
        //     // Filter by Date Range ONLY
        //     orders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay, pageable);
        // }
        
        orders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay, pageable);

        // Convert Entity to DTO
        return orders.map(this::convertToResponse); 
    }


    // ==========================================
    // MANUAL MAPPERS
    // ==========================================

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        
        // 1. Basic Fields
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setOrderType(order.getOrderType());
        response.setSubtotal(order.getSubtotal());
        response.setTaxAmount(order.getTaxAmount());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setTotalAmount(order.getTotalAmount());
        
        // 2. Customer Info
        response.setCustomerName(order.getCustomerName());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setDeliveryAddress(order.getDeliveryAddress());
        
        // 3. Details
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setTableNumber(order.getTableNumber());
        response.setEstimatedPreparationTime(order.getEstimatedPreparationTime());
        
        // 4. Timestamps
        response.setPreparationStartedAt(order.getPreparationStartedAt());
        response.setReadyAt(order.getReadyAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setCancelledAt(order.getCancelledAt());
        response.setCancellationReason(order.getCancellationReason());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // NEW: Rating fields
        response.setRating(order.getRating());
        response.setFeedback(order.getFeedback());
        response.setRatedAt(order.getRatedAt());
        response.setAdminResponse(order.getAdminResponse());
        response.setRespondedAt(order.getRespondedAt());
        
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());    

        // 5. Map Items List (Check for null to avoid errors)
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(this::convertToItemResponse) // Helper method below
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        } else {
            response.setItems(Collections.emptyList());
        }

        // 6. Payment (Optional: Add logic if you have a Payment entity relation)
        // response.setPayment(convertPayment(order.getPayment())); 

        return response;
    }

    private OrderItemResponse convertToItemResponse(OrderItem item) {
        // Ensure you have a similar DTO structure for OrderItemResponse
        OrderItemResponse itemResponse = new OrderItemResponse();
        itemResponse.setId(item.getId());
        itemResponse.setMenuItemName(item.getMenuItemName());
        itemResponse.setQuantity(item.getQuantity());
        itemResponse.setUnitPrice(item.getUnitPrice());
        itemResponse.setSubtotal(item.getSubtotal());
        itemResponse.setSpecialInstructions(item.getSpecialInstructions());

         // NEW: Dish rating fields
        itemResponse.setDishRating(item.getDishRating());
        itemResponse.setDishFeedback(item.getDishFeedback());
        itemResponse.setDishRatedAt(item.getDishRatedAt());
        itemResponse.setAdminDishResponse(item.getAdminDishResponse());
        itemResponse.setDishRespondedAt(item.getDishRespondedAt());
        return itemResponse;
    }

    /**
     * List orders with filters and pagination
     */
    public Page<OrderResponse> listOrders(OrderStatus status, String search, Pageable pageable) {
        Page<Order> orders;
        
       if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else if (search != null && !search.isEmpty()) {
            orders = orderRepository.findByCustomerNameContainingOrOrderNumberContaining(search, search, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        
        return orders.map(this::convertToResponse);
    }

    /**
     * Get orders by specific status (NEW METHOD)
     */
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders;
        
        
        orders = orderRepository.findByStatus(status, pageable);
        
        
        return orders.map(this::convertToResponse);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // 1. Find the order or throw an error if not found
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        // 2. Update the status
        // Note: If you use an Enum for status, convert string to Enum here
        order.setStatus(newStatus);

        // 3. Save and return the updated order
        return orderRepository.save(order);
    }

    // NEW: Get orders with ratings
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersWithRatings(Integer rating, Pageable pageable) {
        log.info(LogCategory.order("Fetching orders with ratings"));
        
        Page<Order> orders;
        if (rating != null) {
            orders = orderRepository.findByRating(rating, pageable);
        } else {
            orders = orderRepository.findByRatingIsNotNull(pageable);
        }
        
        return orders.map(this::convertToResponse);
    }

    // NEW: Submit order rating
    @Transactional
    public OrderResponse submitOrderRating(User user, Long orderId, SubmitOrderRatingRequest request) {
        log.info(LogCategory.order("Submitting rating for order: " + orderId));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed orders");
        }
        
        order.setRating(request.getRating());
        order.setFeedback(request.getFeedback());
        order.setRatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }

    // NEW: Submit dish rating
    @Transactional
    public OrderResponse submitDishRating(User user, Long orderId, Long itemId, SubmitDishRatingRequest request) {
        log.info(LogCategory.order("Submitting dish rating for order: " + orderId + ", item: " + itemId));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed orders");
        }
        
        OrderItem item = order.getOrderItems().stream()
                .filter(oi -> oi.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));
        
        item.setDishRating(request.getDishRating());
        item.setDishFeedback(request.getDishFeedback());
        item.setDishRatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }

    // NEW: Admin respond to order feedback
    @Transactional
    public OrderResponse respondToOrderFeedback(Long orderId, AdminResponseRequest request) {
        log.info(LogCategory.order("Admin responding to order feedback: " + orderId));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (order.getRating() == null) {
            throw new RuntimeException("No feedback to respond to");
        }
        
        order.setAdminResponse(request.getResponse());
        order.setRespondedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }

    // NEW: Admin respond to dish feedback
    @Transactional
    public OrderResponse respondToDishFeedback(Long orderId, Long itemId, AdminResponseRequest request) {
        log.info(LogCategory.order("Admin responding to dish feedback: " + orderId + ", item: " + itemId));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        OrderItem item = order.getOrderItems().stream()
                .filter(oi -> oi.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));
        
        if (item.getDishRating() == null) {
            throw new RuntimeException("No feedback to respond to");
        }
        
        item.setAdminDishResponse(request.getResponse());
        item.setDishRespondedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }



    @Transactional
    public OrderResponse reorder(User user, Long orderId) {
        log.info(LogCategory.order("Re-ordering from order: " + orderId));

        Order originalOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify order belongs to user
        if (!originalOrder.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        // Create new order request from original order
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderType(originalOrder.getOrderType());
        request.setPaymentMethod(com.josephken.roors.payment.entity.PaymentMethod.CASH); // Default to cash for reorder
        request.setCustomerName(originalOrder.getCustomerName());
        request.setCustomerPhone(originalOrder.getCustomerPhone());
        request.setCustomerEmail(originalOrder.getCustomerEmail());
        request.setDeliveryAddress(originalOrder.getDeliveryAddress());
        request.setTableNumber(originalOrder.getTableNumber());

        List<OrderItemRequest> items = originalOrder.getOrderItems().stream()
                .map(item -> new OrderItemRequest(
                        item.getMenuItem().getId(),
                        item.getQuantity(),
                        item.getSpecialInstructions()
                ))
                .collect(Collectors.toList());
        request.setItems(items);

        return createOrder(user, request);
    }

    private OrderResponse mapToResponse(Order order, Payment payment) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItemName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getSpecialInstructions(),

                        item.getDishRating(),
                        item.getDishFeedback(),
                        item.getDishRatedAt(),
                        item.getAdminDishResponse(),
                        item.getDishRespondedAt()
                ))
                .collect(Collectors.toList());

        PaymentResponse paymentResponse = null;
        if (payment != null) {
            paymentResponse = new PaymentResponse(
                    payment.getId(),
                    payment.getPaymentCode(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getPaymentMethod(),
                    payment.getStatus(),
                    payment.getQrCodeData(),
                    payment.getBankAccountNumber(),
                    payment.getBankAccountName(),
                    payment.getBankCode(),
                    payment.getTransactionReference(),
                    payment.getPaidAt(),
                    payment.getExpiresAt(),
                    payment.getNotes(),
                    payment.getCreatedAt()
            );
        }

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getOrderType(),
                items,
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getDeliveryFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getSpecialInstructions(),
                order.getTableNumber(),
                order.getEstimatedPreparationTime(),
                order.getPreparationStartedAt(),
                order.getReadyAt(),
                order.getCompletedAt(),
                order.getCancelledAt(),
                order.getCancellationReason(),
                order.getRating(),
                order.getFeedback(),
                order.getRatedAt(),
                order.getAdminResponse(),
                order.getRespondedAt(),
                paymentResponse,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderResponse mapToResponseWithPayment(Order order, PaymentResponse paymentResponse) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItemName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getSpecialInstructions(),

                        item.getDishRating(),
                        item.getDishFeedback(),
                        item.getDishRatedAt(),
                        item.getAdminDishResponse(),
                        item.getDishRespondedAt()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getOrderType(),
                items,
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getDeliveryFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getSpecialInstructions(),
                order.getTableNumber(),
                order.getEstimatedPreparationTime(),
                order.getPreparationStartedAt(),
                order.getReadyAt(),
                order.getCompletedAt(),
                order.getCancelledAt(),
                order.getCancellationReason(),
                order.getRating(),
                order.getFeedback(),
                order.getRatedAt(),
                order.getAdminResponse(),
                order.getRespondedAt(),
                paymentResponse,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
