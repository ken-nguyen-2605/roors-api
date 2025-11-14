package com.josephken.roors.order.controller;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.order.dto.*;
import com.josephken.roors.order.entity.OrderStatus;
import com.josephken.roors.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(user, request));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByDate(
            @PathVariable String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {

        try {
            LocalDate orderDate = LocalDate.parse(date);
            Page<OrderResponse> orderPage = orderService.getOrdersByDate(
                orderDate, page, size, status
            );
            log.info("Order lookup for date {}: Found {} orders.", date, orderPage.getTotalElements());
            return ResponseEntity.ok(orderPage);
        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided: {}", date);
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {

        Page<OrderResponse> orderPage = orderService.getUserOrders(user, page, size, status);
        log.info("Order lookup for user {}: Found {} total orders.", user.getId(), orderPage.getTotalElements());
        return ResponseEntity.ok(orderPage);
    }
    
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderResponse> orders = orderService.listOrders(status, search, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderResponse> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    // NEW: Get orders with ratings
    @GetMapping("/with-ratings")
    public ResponseEntity<Page<OrderResponse>> getOrdersWithRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "ratedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Integer rating) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderResponse> orders = orderService.getOrdersWithRatings(rating, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(user, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(user, id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(user, id, request));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<OrderResponse> reorder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.reorder(user, id));
    }

    @PutMapping("/{orderId}/status")    
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            var updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
            response.put("success", true);
            response.put("message", "Order status updated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") 
                              ? HttpStatus.NOT_FOUND 
                              : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    // NEW: Submit order rating
    @PostMapping("/{orderId}/rating")
    public ResponseEntity<OrderResponse> submitOrderRating(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @Valid @RequestBody SubmitOrderRatingRequest request) {
        return ResponseEntity.ok(orderService.submitOrderRating(user, orderId, request));
    }

    // NEW: Submit dish rating
    @PostMapping("/{orderId}/items/{itemId}/rating")
    public ResponseEntity<OrderResponse> submitDishRating(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody SubmitDishRatingRequest request) {
        return ResponseEntity.ok(orderService.submitDishRating(user, orderId, itemId, request));
    }

    // NEW: Admin respond to order feedback
    @PostMapping("/{orderId}/rating/response")
    public ResponseEntity<OrderResponse> respondToOrderFeedback(
            @PathVariable Long orderId,
            @Valid @RequestBody AdminResponseRequest request) {
        return ResponseEntity.ok(orderService.respondToOrderFeedback(orderId, request));
    }

    // NEW: Admin respond to dish feedback
    @PostMapping("/{orderId}/items/{itemId}/rating/response")
    public ResponseEntity<OrderResponse> respondToDishFeedback(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody AdminResponseRequest request) {
        return ResponseEntity.ok(orderService.respondToDishFeedback(orderId, itemId, request));
    }
}