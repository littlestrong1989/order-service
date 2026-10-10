package com.demo.order.repository;

import com.demo.order.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p from Product p
            where (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%')))
              and (:active is null or p.active = :active)
            """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("active") Boolean active,
                         Pageable pageable);
}
