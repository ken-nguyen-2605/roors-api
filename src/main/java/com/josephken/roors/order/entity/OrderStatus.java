package com.josephken.roors.order.entity;

public enum OrderStatus {
    PENDING,        // Order placed, waiting for confirmation
    CONFIRMED,      // Order confirmed by restaurant
    PREPARING,      // Kitchen is preparing the order
    READY,          // Order is ready for pickup/delivery
    OUT_FOR_DELIVERY, // For delivery orders only
    COMPLETED,      // Order delivered/picked up
    CANCELLED       // Order cancelled
}
