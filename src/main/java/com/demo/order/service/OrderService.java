package com.demo.order.service;

import com.demo.order.common.BizException;
import com.demo.order.common.PageResult;
import com.demo.order.dto.OrderCreateRequest;
import com.demo.order.dto.OrderItemRequest;
import com.demo.order.dto.OrderQuery;
import com.demo.order.entity.Order;
import com.demo.order.entity.OrderItem;
import com.demo.order.entity.Product;
import com.demo.order.repository.OrderRepository;
import com.demo.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * 下单：校验商品 -> 扣减库存（乐观锁防超卖）-> 生成订单快照
     * 整体在一个事务里，任一商品库存不足即整单失败回滚。
     */
    @Transactional
    public Order create(String username, OrderCreateRequest req) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUsername(username);

        for (OrderItemRequest item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> BizException.notFound("商品不存在: " + item.getProductId()));

            if (Boolean.FALSE.equals(product.getActive())) {
                throw BizException.conflict("商品已下架: " + product.getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw BizException.conflict("库存不足: " + product.getName()
                        + "（剩余 " + product.getStock() + "）");
            }

            // 扣库存。@Version 乐观锁：并发下若版本已变，抛 OptimisticLockingFailureException -> 全局处理为 409
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.saveAndFlush(product);

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());     // 名称快照
            oi.setPrice(product.getPrice());          // 价格快照
            oi.setQuantity(item.getQuantity());
            oi.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            order.addItem(oi);
        }
        return orderRepository.save(order);
    }

    public Order getByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> BizException.notFound("订单不存在"));
    }

    /** 普通用户只能看自己的订单；ADMIN 可看全部 */
    public PageResult<Order> page(OrderQuery q, String currentUser, boolean isAdmin) {
        String username = isAdmin ? blankToNull(q.getUsername()) : currentUser;
        PageRequest pageable = PageRequest.of(
                Math.max(q.getPage() - 1, 0),
                Math.min(Math.max(q.getSize(), 1), 100),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> result = orderRepository.search(username, q.getStatus(), pageable);
        return PageResult.of(result);
    }

    @Transactional
    public Order cancel(Long id, String currentUser, boolean isAdmin) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        if (!isAdmin && !order.getUsername().equals(currentUser)) {
            throw new BizException(403, "无权操作该订单");
        }
        if (order.getStatus() != com.demo.order.entity.OrderStatus.CREATED) {
            throw BizException.conflict("仅待支付订单可取消，当前状态: " + order.getStatus());
        }

        // 回滚库存
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                p.setStock(p.getStock() + item.getQuantity());
                productRepository.save(p);
            });
        }
        order.setStatus(com.demo.order.entity.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private static String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
