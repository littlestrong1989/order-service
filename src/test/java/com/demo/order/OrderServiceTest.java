package com.demo.order;

import com.demo.order.common.BizException;
import com.demo.order.dto.OrderCreateRequest;
import com.demo.order.dto.OrderItemRequest;
import com.demo.order.entity.Order;
import com.demo.order.entity.OrderStatus;
import com.demo.order.entity.Product;
import com.demo.order.repository.OrderRepository;
import com.demo.order.repository.ProductRepository;
import com.demo.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 订单业务逻辑测试：库存扣减 / 超卖拦截 / 取消回滚 */
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;

    private Long keyboardId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        Product p = new Product();
        p.setName("机械键盘 K8");
        p.setPrice(new BigDecimal("399.00"));
        p.setStock(5);
        p.setActive(true);
        keyboardId = productRepository.save(p).getId();
    }

    private OrderCreateRequest req(int qty) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(keyboardId);
        item.setQuantity(qty);
        OrderCreateRequest r = new OrderCreateRequest();
        r.setItems(List.of(item));
        return r;
    }

    @Test
    @DisplayName("下单成功：扣减库存 + 计算金额 + 商品快照")
    void createOrder_shouldDeductStockAndCalcTotal() {
        Order order = orderService.create("user", req(2));

        assertThat(order.getOrderNo()).startsWith("ORD");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("798.00"); // 399 * 2
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getProductName()).isEqualTo("机械键盘 K8");
        assertThat(order.getItems().get(0).getSubtotal()).isEqualByComparingTo("798.00");

        Product after = productRepository.findById(keyboardId).orElseThrow();
        assertThat(after.getStock()).isEqualTo(3); // 5 - 2
    }

    @Test
    @DisplayName("库存不足：整单拒绝，且库存不变")
    void createOrder_insufficientStock_shouldReject() {
        assertThatThrownBy(() -> orderService.create("user", req(999)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("库存不足");

        assertThat(productRepository.findById(keyboardId).orElseThrow().getStock()).isEqualTo(5);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    @DisplayName("取消订单：库存回滚")
    void cancelOrder_shouldRestoreStock() {
        Order order = orderService.create("user", req(2));
        assertThat(productRepository.findById(keyboardId).orElseThrow().getStock()).isEqualTo(3);

        Order cancelled = orderService.cancel(order.getId(), "user", false);

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productRepository.findById(keyboardId).orElseThrow().getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("非本人且非管理员：不能取消他人订单")
    void cancelOrder_notOwner_shouldDeny() {
        Order order = orderService.create("user", req(1));
        assertThatThrownBy(() -> orderService.cancel(order.getId(), "other", false))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("无权");
    }
}
