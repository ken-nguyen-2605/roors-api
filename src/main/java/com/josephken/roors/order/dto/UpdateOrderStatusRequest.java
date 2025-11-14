package com.josephken.roors.order.dto;
import com.josephken.roors.order.entity.OrderStatus;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
public class UpdateOrderStatusRequest {
    private OrderStatus status;

}