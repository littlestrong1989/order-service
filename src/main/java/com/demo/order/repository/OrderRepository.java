package com.demo.order.repository;

import com.demo.order.entity.Order;
import com.demo.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNo(String orderNo);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);

    @Query("""
            select distinct o from Order o left join fetch o.items
            where (:username is null or o.username = :username)
              and (:status is null or o.status = :status)
            """)
    Page<Order> search(@Param("username") String username,
                       @Param("status") OrderStatus status,
                       Pageable pageable);
}
