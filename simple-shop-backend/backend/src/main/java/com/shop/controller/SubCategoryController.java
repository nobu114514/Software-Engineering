package com.shop.controller;

import com.shop.dto.SubCategoryDTO;
import com.shop.model.SubCategory;
import com.shop.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/subcategories", "/api/sub-categories"})
public class SubCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(SubCategoryController.class);

    @Autowired
    private SubCategoryService subCategoryService;

    // 根据一级分类ID获取启用的二级分类
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubCategory>> getActiveSubCategoriesByCategory(@PathVariable Long categoryId) {
        logger.info("Get subcategories for category ID: {}", categoryId);
        try {
            List<SubCategory> subCategories = subCategoryService.getActiveSubCategoriesByCategoryId(categoryId);
            // 清除分类引用以避免循环依赖
            for (SubCategory subCategory : subCategories) {
                if (subCategory.getCategory() != null) {
                    subCategory.setCategory(null);
                }
            }
            return ResponseEntity.ok(subCategories);
        } catch (Exception e) {
            logger.error("Error getting subcategories by category id", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 获取所有启用的二级分类
    @GetMapping("/active")
    public ResponseEntity<List<SubCategory>> getAllActiveSubCategories() {
        logger.info("Get all active subcategories");
        try {
            List<SubCategory> subCategories = subCategoryService.getAllActiveSubCategories();
            // 清除分类引用以避免循环依赖
            for (SubCategory subCategory : subCategories) {
                if (subCategory.getCategory() != null) {
                    subCategory.setCategory(null);
                }
            }
            return ResponseEntity.ok(subCategories);
        } catch (Exception e) {
            logger.error("Error getting all active subcategories", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 根据ID获取二级分类
    @GetMapping("/{id}")
    public ResponseEntity<SubCategory> getSubCategoryById(@PathVariable Long id) {
        logger.info("Get subcategory by ID: {}", id);
        try {
            return subCategoryService.getSubCategoryById(id)
                .map(subCategory -> {
                    // 清除分类引用以避免循环依赖
                    if (subCategory.getCategory() != null) {
                        subCategory.setCategory(null);
                    }
                    return ResponseEntity.ok(subCategory);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting subcategory by id", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 创建新二级分类
    @PostMapping
    public ResponseEntity<?> createSubCategory(@RequestBody SubCategory subCategory) {
        logger.info("Create subcategory: {}", subCategory.getName());
        try {
            SubCategory created = subCategoryService.createSubCategory(subCategory);
            // 清除分类引用以避免循环依赖
            if (created.getCategory() != null) {
                created.setCategory(null);
            }
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error creating subcategory", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 更新二级分类
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubCategory(@PathVariable Long id, @RequestBody SubCategory subCategory) {
        logger.info("Update subcategory: {}", id);
        try {
            subCategory.setId(id);
            SubCategory updated = subCategoryService.updateSubCategory(subCategory);
            // 清除分类引用以避免循环依赖
            if (updated.getCategory() != null) {
                updated.setCategory(null);
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error updating subcategory", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 删除二级分类
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSubCategory(@PathVariable Long id) {
        logger.info("Delete subcategory: {}", id);
        Map<String, Object> response = new HashMap<>();
        try {
            subCategoryService.deleteSubCategory(id);
            response.put("status", "success");
            response.put("message", "SubCategory deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting subcategory", e);
            response.put("status", "error");
            response.put("message", "Failed to delete subcategory");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 商家后台：创建二级分类
    @PostMapping("/seller/sub-categories")
    public ResponseEntity<?> createSubCategoryForSeller(@RequestBody SubCategoryDTO subCategoryDTO) {
        logger.info("Seller create subcategory: {}", subCategoryDTO.getName());
        try {
            SubCategoryDTO created = subCategoryService.createSubCategory(subCategoryDTO);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error creating subcategory for seller", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 商家后台：更新二级分类
    @PutMapping("/seller/sub-categories/{id}")
    public ResponseEntity<?> updateSubCategoryForSeller(@PathVariable Long id, @RequestBody SubCategoryDTO subCategoryDTO) {
        logger.info("Seller update subcategory: {}", id);
        try {
            SubCategoryDTO updated = subCategoryService.updateSubCategory(id, subCategoryDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error updating subcategory for seller", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 商家后台：获取所有二级分类列表
    @GetMapping("/seller/sub-categories")
    public ResponseEntity<Map<String, Object>> getAllSellerSubCategories() {
        logger.info("获取商家所有二级分类列表");
        Map<String, Object> response = new HashMap<>();
        try {
            List<SubCategoryDTO> subCategories = subCategoryService.getAllSubCategoryDTOs();
            response.put("success", true);
            response.put("data", subCategories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取二级分类列表失败", e);
            response.put("success", false);
            response.put("message", "获取二级分类列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 商家后台：根据一级分类ID获取二级分类列表
    @GetMapping("/seller/sub-categories/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getSellerSubCategoriesByCategory(@PathVariable Long categoryId) {
        logger.info("获取商家指定一级分类的二级分类列表，分类ID: {}", categoryId);
        Map<String, Object> response = new HashMap<>();
        try {
            List<SubCategoryDTO> subCategories = subCategoryService.getSubCategoryDTOsByCategoryId(categoryId);
            response.put("success", true);
            response.put("data", subCategories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取指定分类的二级分类列表失败", e);
            response.put("success", false);
            response.put("message", "获取二级分类列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 商家后台：获取二级分类详情
    @GetMapping("/seller/sub-categories/{id}")
    public ResponseEntity<?> getSubCategoryForSeller(@PathVariable Long id) {
        logger.info("Seller get subcategory by ID: {}", id);
        try {
            return subCategoryService.getSubCategoryDTOById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting subcategory by id for seller", e);
            return ResponseEntity.status(500).build();
        }
    }
}