package com.josephken.roors.payment.entity;

public enum SePayProcessingStatus {
    RECEIVED,           // Vừa nhận webhook
    MATCHED,            // Đã match với Payment
    COMPLETED,          // Đã xử lý xong, cập nhật Payment thành công
    NO_MATCH,           // Không tìm thấy Payment tương ứng
    AMOUNT_MISMATCH,    // Số tiền không khớp
    EXPIRED,            // Payment đã hết hạn
    DUPLICATE,          // Webhook trùng lặp (đã xử lý trước đó)
    FAILED              // Lỗi xử lý
}