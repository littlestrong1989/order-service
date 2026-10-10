package com.demo.order.dto;

import com.demo.order.entity.OrderStatus;
import lombok.Data;

@Data
public class OrderQuery {
    /** 仅 ADMIN 可用；普通用户强制只看自己的订单 */
    private String username;
    private OrderStatus status;
    private int page = 1;
    private int size = 10;
}
