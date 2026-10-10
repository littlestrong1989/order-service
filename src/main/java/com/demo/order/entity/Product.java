package com.demo.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品。使用 @Version 乐观锁，防止并发下单超卖 */
@Entity
@Table(name = "product", indexes = @Index(name = "idx_product_name", columnList = "name"))
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** 库存 */
    @Column(nullable = false)
    private Integer stock;

    /** 乐观锁版本号 */
    @Version
    private Long version;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
