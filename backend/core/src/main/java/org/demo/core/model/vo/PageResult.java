package org.demo.core.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页结果VO
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    public static <T> PageResult<T> of(List<T> list, Long total) {
        return new PageResult<>(list, total);
    }
}
