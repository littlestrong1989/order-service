package com.demo.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 订单项（下单时快照商品名与单价，避免商品后续改价影响历史订单） */
@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属订单。
     * 加 @JsonIgnore 打断 Order <-> OrderItem 的双向循环引用，
     * 否则序列化会无限递归（曾导致响应体深层嵌套、JsonPath 解析失败）。
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 128)
    private String productName;

    /** 下单时单价快照 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}
