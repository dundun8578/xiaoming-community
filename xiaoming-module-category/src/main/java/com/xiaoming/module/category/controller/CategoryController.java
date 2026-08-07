package com.xiaoming.module.category.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.category.dto.CategoryAddDTO;
import com.xiaoming.module.category.dto.CategoryStatusDTO;
import com.xiaoming.module.category.service.CategoryService;
import com.xiaoming.module.category.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
/**
 * 分类控制器
 */
@Tag(name = "分类管理", description = "笔记分类管理接口")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有启用的分类列表
     */
    @Operation(summary = "获取启用分类列表")
    @GetMapping("/list")
    public Result<List<CategoryVO>> listEnabledCategories() {
        List<CategoryVO> list = categoryService.listEnabledCategories();
        return Result.ok(list);
    }

    /**
     * 获取所有分类列表(管理员)
     */
    @Operation(summary = "获取所有分类列表(管理员)")
    @GetMapping("/list/all")
    public Result<List<CategoryVO>> listAllCategories() {
        List<CategoryVO> list = categoryService.listAllCategories();
        return Result.ok(list);
    }

    /**
     * 根据ID查询分类详情
     */
    @Operation(summary = "查询分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        CategoryVO vo = categoryService.getCategoryById(id);
        if (vo == null) {
            return Result.error(404, "分类不存在");
        }
        return Result.ok(vo);
    }

    /**
     * 新增分类(管理员)
     */
    @Operation(summary = "新增分类(管理员)")
    @PostMapping("/add")
    public Result<Void> addCategory(@Valid @RequestBody CategoryAddDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setIcon(dto.getIcon() != null ? dto.getIcon() : "");
        category.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(1);

        boolean success = categoryService.addCategory(category);
        return success ? Result.ok() : Result.error(500, "新增失败");
    }

    /**
     * 更新分类(管理员)
     */
    @Operation(summary = "更新分类(管理员)")
    @PutMapping("/update")
    public Result<Void> updateCategory(@Valid @RequestBody CategoryAddDTO dto) {
        if (dto.getId() == null) {
            return Result.error(400, "分类ID不能为空");
        }
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon() != null ? dto.getIcon() : "");
        category.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);

        boolean success = categoryService.updateCategory(category);
        return success ? Result.ok() : Result.error(500, "更新失败");
    }

    /**
     * 更新分类状态(管理员)
     */
    @Operation(summary = "更新分类状态(管理员)")
    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody CategoryStatusDTO dto) {
        boolean success = categoryService.updateStatus(dto.getId(), dto.getStatus());
        return success ? Result.ok() : Result.error(500, "更新失败");
    }

    /**
     * 删除分类(管理员,软删除)
     */
    @Operation(summary = "删除分类(管理员)")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        boolean success = categoryService.deleteCategory(id);
        return success ? Result.ok() : Result.error(500, "删除失败");
    }
}
