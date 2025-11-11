package com.shop.controller;

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
@RequestMapping("/api/categories")
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
}