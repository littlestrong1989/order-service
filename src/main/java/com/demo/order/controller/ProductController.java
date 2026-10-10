package com.demo.order.controller;

import com.demo.order.common.ApiResponse;
import com.demo.order.common.PageResult;
import com.demo.order.dto.ProductQuery;
import com.demo.order.dto.ProductRequest;
import com.demo.order.entity.Product;
import com.demo.order.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页检索商品（公开）")
    @GetMapping
    public ApiResponse<PageResult<Product>> page(ProductQuery query) {
        return ApiResponse.ok(productService.page(query));
    }

    @Operation(summary = "商品详情（公开）")
    @GetMapping("/{id}")
    public ApiResponse<Product> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.get(id));
    }

    @Operation(summary = "新增商品（仅 ADMIN）")
    @PostMapping
    public ApiResponse<Product> create(@Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok(productService.create(req));
    }

    @Operation(summary = "修改商品（仅 ADMIN）")
    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok(productService.update(id, req));
    }

    @Operation(summary = "下架商品（仅 ADMIN，软删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok();
    }
}
