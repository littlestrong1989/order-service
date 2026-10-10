package com.demo.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    /** ROLE_ADMIN / ROLE_USER */
    @Column(nullable = false, length = 32)
    private String role = "ROLE_USER";

    private LocalDateTime createdAt = LocalDateTime.now();
}
