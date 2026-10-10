package com.demo.order.dto;

import lombok.Data;

@Data
public class ProductQuery {
    private String keyword;
    private Boolean active;
    private int page = 1;
    private int size = 10;
    private String sort = "id,desc";
}
