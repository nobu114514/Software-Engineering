package com.shop.service;

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
}