package com.demo.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 订单（"order" 是 SQL 保留字，表名用 orders） */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_no", columnList = "orderNo", unique = true),
        @Index(name = "idx_order_user", columnList = "username")
})
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String orderNo;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status = OrderStatus.CREATED;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getSubtotal());
    }
}
