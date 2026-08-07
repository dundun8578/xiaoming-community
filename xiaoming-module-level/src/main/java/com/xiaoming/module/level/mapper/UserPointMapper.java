package com.xiaoming.module.level.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoming.module.level.entity.UserPoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分Mapper
 */
@Mapper
public interface UserPointMapper extends BaseMapper<UserPoint> {
}
