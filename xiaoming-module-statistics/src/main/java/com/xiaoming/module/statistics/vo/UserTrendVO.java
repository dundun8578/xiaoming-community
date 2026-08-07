package com.xiaoming.module.statistics.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用户统计趋势VO
 */
@Data
public class UserTrendVO {

    /**
     * 日期列表 (最近30天)
     */
    private List<String> dates;

    /**
     * 新增用户趋势
     */
    private List<Long> newUsers;

    /**
     * 活跃用户趋势
     */
    private List<Long> activeUsers;

    /**
     * 累计用户数趋势
     */
    private List<Long> totalUsers;
}
