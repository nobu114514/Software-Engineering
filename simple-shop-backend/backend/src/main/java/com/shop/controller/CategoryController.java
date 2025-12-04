package com.shop.controller;

import com.shop.dto.CategoryDTO;
import com.shop.model.Category;
import com.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/categories", "/api/categories"})
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/test")
    public Map<String, Object> testEndpoint() {
        logger.info("Test endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Categories API is working!");
        return response;
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveCategories() {
        logger.info("Active categories endpoint called");
        Map<String, Object> response = new HashMap<>();
        try {
            List<Category> categories = categoryService.getAllActiveCategories();
            // 清除子分类引用以避免循环依赖
            for (Category category : categories) {
                if (category.getSubCategories() != null) {
                    category.setSubCategories(null);
                }
            }
            response.put("status", "success");
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting active categories", e);
            response.put("status", "error");
            response.put("message", "Failed to get categories");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        logger.info("All categories endpoint called");
        Map<String, Object> response = new HashMap<>();
        try {
            List<Category> categories = categoryService.getAllCategories();
            // 清除子分类引用以避免循环依赖
            for (Category category : categories) {
                if (category.getSubCategories() != null) {
                    category.setSubCategories(null);
                }
            }
            response.put("status", "success");
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all categories", e);
            response.put("status", "error");
            response.put("message", "Failed to get categories");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        logger.info("Get category by id: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            return categoryService.getCategoryById(id)
                    .map(category -> {
                        // 清除子分类引用以避免循环依赖
                        if (category.getSubCategories() != null) {
                            category.setSubCategories(null);
                        }
                        response.put("status", "success");
                        response.put("data", category);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("status", "error");
                        response.put("message", "Category not found");
                        return ResponseEntity.status(404).body(response);
                    });
        } catch (Exception e) {
            logger.error("Error getting category by id", e);
            response.put("status", "error");
            response.put("message", "Failed to get category");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        logger.info("Create category: {}", category.getName());
        Map<String, Object> response = new HashMap<>();
        try {
            // 清除子分类引用以避免循环依赖
            if (category.getSubCategories() != null) {
                category.setSubCategories(null);
            }
            Category created = categoryService.createCategory(category);
            // 确保返回的对象也没有子分类引用
            if (created.getSubCategories() != null) {
                created.setSubCategories(null);
            }
            response.put("status", "success");
            response.put("data", created);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("Error creating category", e);
            response.put("status", "error");
            response.put("message", "Failed to create category");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        logger.info("Update category: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            category.setId(id);
            // 清除子分类引用以避免循环依赖
            if (category.getSubCategories() != null) {
                category.setSubCategories(null);
            }
            Category updated = categoryService.updateCategory(category);
            // 确保返回的对象也没有子分类引用
            if (updated.getSubCategories() != null) {
                updated.setSubCategories(null);
            }
            response.put("status", "success");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating category", e);
            response.put("status", "error");
            response.put("message", "Failed to update category");
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        logger.info("Delete category: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            categoryService.deleteCategory(id);
            response.put("status", "success");
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting category", e);
            response.put("status", "error");
            response.put("message", "Failed to delete category");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 商家后台分类管理API
    @PostMapping("/seller/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody CategoryDTO categoryDTO) {
        logger.info("商家创建分类: {}", categoryDTO.getName());
        Map<String, Object> response = new HashMap<>();
        try {
            CategoryDTO created = categoryService.createCategory(categoryDTO);
            response.put("success", true);
            response.put("data", created);
            response.put("message", "分类创建成功");
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("创建分类失败", e);
            response.put("success", false);
            response.put("message", "创建分类失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/seller/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        logger.info("商家更新分类: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            CategoryDTO updated = categoryService.updateCategory(id, categoryDTO);
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "分类更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新分类失败", e);
            response.put("success", false);
            response.put("message", "更新分类失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 商家后台：获取所有分类列表
    @GetMapping("/seller/categories")
    public ResponseEntity<Map<String, Object>> getSellerCategories() {
        logger.info("获取商家分类列表");
        Map<String, Object> response = new HashMap<>();
        try {
            List<CategoryDTO> categories = categoryService.getAllCategoryDTOs();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取分类列表失败", e);
            response.put("success", false);
            response.put("message", "获取分类列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/seller/categories/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryDetail(@PathVariable Long id) {
        logger.info("获取分类详情: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            CategoryDTO category = categoryService.getCategoryDTOById(id);
            response.put("success", true);
            response.put("data", category);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting category details", e);
            response.put("success", false);
            response.put("message", "获取分类详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}