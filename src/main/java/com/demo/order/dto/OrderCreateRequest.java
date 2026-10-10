package com.demo.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    @NotEmpty(message = "不能为空")
    @Size(max = 50, message = "一次最多 50 个商品")
    @Valid
    private List<OrderItemRequest> items;
}
