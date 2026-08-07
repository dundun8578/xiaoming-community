package com.xiaoming.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaoming.module.category.entity.Category;
import com.xiaoming.module.category.mapper.CategoryMapper;
import com.xiaoming.module.category.service.CategoryService;
import com.xiaoming.module.category.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryVO> listEnabledCategories() {
        LambdaQueryWrapper<Category> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Category::getStatus, 1)
               .eq(Category::getIsDeleted, 0)
               .orderByAsc(Category::getSort);

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listAllCategories() {
        LambdaQueryWrapper<Category> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Category::getIsDeleted, 0)
               .orderByAsc(Category::getSort);

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addCategory(Category category) {
        category.setIsDeleted(0);
        category.setNoteCount(0);
        return this.save(category);
    }

    @Override
    public boolean updateCategory(Category category) {
        category.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(category);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setIsDeleted(1);
        category.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(category);
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = this.getById(id);
        if (category == null || category.getIsDeleted() == 1) {
            return null;
        }
        return convertToVO(category);
    }

    /**
     * 转换为VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setDescription(category.getDescription());
        vo.setNoteCount(category.getNoteCount());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        return vo;
    }
}
