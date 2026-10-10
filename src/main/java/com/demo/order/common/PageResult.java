package com.demo.order.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/** 统一分页结果 */
@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(Page<T> p) {
        PageResult<T> r = new PageResult<>();
        r.list = p.getContent();
        r.total = p.getTotalElements();
        r.page = p.getNumber() + 1;
        r.size = p.getSize();
        return r;
    }
}
