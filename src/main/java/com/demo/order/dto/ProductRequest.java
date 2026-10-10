package com.demo.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 128, message = "长度不能超过 128")
    private String name;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.01", message = "必须大于 0")
    private BigDecimal price;

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "不能为负数")
    private Integer stock;

    private Boolean active = Boolean.TRUE;
}
