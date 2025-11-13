package com.shop.service.impl;

import com.shop.dto.CategoryDTO;
import com.shop.model.Category;
import com.shop.repository.CategoryRepository;
import com.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        updateCategoryFromDTO(category, categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        updateCategoryFromDTO(category, categoryDTO);
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    public CategoryDTO getCategoryDTOById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        return convertToDTO(category);
    }
    
    @Override
    public List<CategoryDTO> getAllCategoryDTOs() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.isActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setIcon(category.getIcon());
        return dto;
    }

    private void updateCategoryFromDTO(Category entity, CategoryDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());
        entity.setSortOrder(dto.getSortOrder());
        entity.setIcon(dto.getIcon());
    }
}