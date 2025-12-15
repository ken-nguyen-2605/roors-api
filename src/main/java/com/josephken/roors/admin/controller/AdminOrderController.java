package com.josephken.roors.admin.controller;

import com.josephken.roors.order.dto.OrderResponse;
import com.josephken.roors.order.entity.OrderStatus;
import com.josephken.roors.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')") // Temporarily disabled for testing
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
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
        log.info("Admin retrieved {} orders", orders.getTotalElements());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderResponse> orders = orderService.getOrdersByStatus(status, pageable);
        log.info("Admin retrieved {} orders with status {}", orders.getTotalElements(), status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        OrderResponse updatedOrder = orderService.updateOrderStatusAndReturn(orderId, status);
        log.info("Admin updated order {} status to {}", orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByDate(
            @PathVariable String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        try {
            LocalDate orderDate = LocalDate.parse(date);
            Page<OrderResponse> orderPage = orderService.getOrdersByDate(
                orderDate, page, size, status
            );
            log.info("Admin retrieved {} orders for date {}", orderPage.getTotalElements(), date);
            return ResponseEntity.ok(orderPage);
        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided: {}", date);
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD");
        }
    }
}