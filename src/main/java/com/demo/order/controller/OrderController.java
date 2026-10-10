package com.demo.order.controller;

import com.demo.order.common.ApiResponse;
import com.demo.order.common.PageResult;
import com.demo.order.dto.OrderCreateRequest;
import com.demo.order.dto.OrderQuery;
import com.demo.order.entity.Order;
import com.demo.order.service.CurrentUserService;
import com.demo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "下单（扣减库存，事务 + 乐观锁防超卖）")
    @PostMapping
    public ApiResponse<Order> create(@Valid @RequestBody OrderCreateRequest req) {
        return ApiResponse.ok(orderService.create(currentUserService.username(), req));
    }

    @Operation(summary = "订单列表（用户仅见自己；ADMIN 可查全部）")
    @GetMapping
    public ApiResponse<PageResult<Order>> page(OrderQuery query) {
        return ApiResponse.ok(orderService.page(
                query, currentUserService.username(), currentUserService.isAdmin()));
    }

    @Operation(summary = "订单详情（按订单号）")
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> detail(@PathVariable String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        // 非管理员只能看自己的订单
        if (!currentUserService.isAdmin()
                && !order.getUsername().equals(currentUserService.username())) {
            throw new com.demo.order.common.BizException(403, "无权查看该订单");
        }
        return ApiResponse.ok(order);
    }

    @Operation(summary = "取消订单（回滚库存）")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Order> cancel(@PathVariable Long id) {
        return ApiResponse.ok(orderService.cancel(
                id, currentUserService.username(), currentUserService.isAdmin()));
    }
}
