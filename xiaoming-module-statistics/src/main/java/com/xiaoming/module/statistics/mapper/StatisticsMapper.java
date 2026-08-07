package com.xiaoming.module.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoming.module.statistics.entity.Statistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统计数据Mapper
 */
@Mapper
public interface StatisticsMapper extends BaseMapper<Statistics> {
}
