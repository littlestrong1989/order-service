package com.demo.order.service;

import com.demo.order.common.BizException;
import com.demo.order.common.PageResult;
import com.demo.order.dto.ProductQuery;
import com.demo.order.dto.ProductRequest;
import com.demo.order.entity.Product;
import com.demo.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public PageResult<Product> page(ProductQuery q) {
        PageRequest pageable = PageRequest.of(
                Math.max(q.getPage() - 1, 0),
                Math.min(Math.max(q.getSize(), 1), 100),
                parseSort(q.getSort()));
        Page<Product> result = repository.search(
                blankToNull(q.getKeyword()), q.getActive(), pageable);
        return PageResult.of(result);
    }

    /** 单商品读多写少，加缓存 */
    @Cacheable(value = "product", key = "#id")
    public Product get(Long id) {
        return repository.findById(id).orElseThrow(() -> BizException.notFound("商品不存在"));
    }

    @Transactional
    @CacheEvict(value = "product", key = "#result.id")
    public Product create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        return repository.save(p);
    }

    @Transactional
    @CacheEvict(value = "product", key = "#id")
    public Product update(Long id, ProductRequest req) {
        Product p = get(id);
        apply(p, req);
        return repository.save(p);
    }

    @Transactional
    @CacheEvict(value = "product", key = "#id")
    public void delete(Long id) {
        Product p = repository.findById(id).orElseThrow(() -> BizException.notFound("商品不存在"));
        p.setActive(false); // 软删除，保留历史订单引用
        repository.save(p);
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.getName());
        p.setPrice(r.getPrice());
        p.setStock(r.getStock());
        p.setActive(r.getActive() == null || r.getActive());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.DESC, "id");
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
