package com.shop.service;

import com.shop.dto.CategoryDTO;
import com.shop.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> getAllActiveCategories();

    List<Category> getAllCategories();

    Optional<Category> getCategoryById(Long id);

    Category createCategory(Category category);

    Category updateCategory(Category category);

    void deleteCategory(Long id);
    
    // DTO方法
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    
    CategoryDTO getCategoryDTOById(Long id);
    
    List<CategoryDTO> getAllCategoryDTOs();
}