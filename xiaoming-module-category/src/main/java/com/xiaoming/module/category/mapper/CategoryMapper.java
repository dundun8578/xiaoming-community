package com.xiaoming.module.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoming.module.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
