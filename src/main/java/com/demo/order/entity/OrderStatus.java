package com.demo.order.entity;

public enum OrderStatus {
    /** 已创建（待支付） */
    CREATED,
    /** 已支付 */
    PAID,
    /** 已取消 */
    CANCELLED
}
