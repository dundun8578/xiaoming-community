package com.xiaoming.common.page;

import lombok.Data;
import java.io.Serializable;

/**
 * 分页请求参数
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方式: asc/desc
     */
    private String sortOrder = "desc";
}
