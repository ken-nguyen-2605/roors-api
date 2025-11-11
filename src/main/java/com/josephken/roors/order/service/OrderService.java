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
import java.util.List;
import java.util.stream.Collectors;

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
        }

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
            order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Order is already being prepared and cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(request.getReason());

        Order cancelledOrder = orderRepository.save(order);
        log.info(LogCategory.order("Order cancelled successfully: " + orderId));

        return mapToResponse(cancelledOrder, null);
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
                        item.getSpecialInstructions()
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
                        item.getSpecialInstructions()
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
                paymentResponse,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
