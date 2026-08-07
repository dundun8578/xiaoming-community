package com.xiaoming.module.category.service;

import com.xiaoming.module.category.entity.Category;
import com.xiaoming.module.category.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取所有启用的分类列表
     */
    List<CategoryVO> listEnabledCategories();

    /**
     * 获取所有分类列表(包括禁用)
     */
    List<CategoryVO> listAllCategories();

    /**
     * 新增分类
     */
    boolean addCategory(Category category);

    /**
     * 更新分类
     */
    boolean updateCategory(Category category);

    /**
     * 更新分类状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 删除分类
     */
    boolean deleteCategory(Long id);

    /**
     * 根据ID查询分类
     */
    CategoryVO getCategoryById(Long id);
}
