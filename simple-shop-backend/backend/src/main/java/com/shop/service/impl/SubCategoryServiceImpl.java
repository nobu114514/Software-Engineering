package com.shop.service.impl;

import com.shop.model.SubCategory;
import com.shop.repository.SubCategoryRepository;
import com.shop.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Override
    public List<SubCategory> getActiveSubCategoriesByCategoryId(Long categoryId) {
        return subCategoryRepository.findByCategoryIdAndActiveTrueOrderBySortOrderAsc(categoryId);
    }

    @Override
    public List<SubCategory> getAllActiveSubCategories() {
        return subCategoryRepository.findByActiveTrue();
    }

    @Override
    public Optional<SubCategory> getSubCategoryById(Long id) {
        return subCategoryRepository.findById(id);
    }

    @Override
    public SubCategory createSubCategory(SubCategory subCategory) {
        // 检查分类下是否已存在同名二级分类
        if (subCategoryRepository.existsByCategoryIdAndName(
                subCategory.getCategory().getId(), subCategory.getName())) {
            throw new IllegalArgumentException("该分类下已存在同名二级分类");
        }
        return subCategoryRepository.save(subCategory);
    }

    @Override
    public SubCategory updateSubCategory(SubCategory subCategory) {
        // 检查是否与其他同级二级分类重名
        Optional<SubCategory> existing = subCategoryRepository.findById(subCategory.getId());
        if (existing.isPresent() && (
                !existing.get().getName().equals(subCategory.getName()) ||
                !existing.get().getCategory().getId().equals(subCategory.getCategory().getId())) &&
                subCategoryRepository.existsByCategoryIdAndName(
                        subCategory.getCategory().getId(), subCategory.getName())) {
            throw new IllegalArgumentException("该分类下已存在同名二级分类");
        }
        return subCategoryRepository.save(subCategory);
    }

    @Override
    public void deleteSubCategory(Long id) {
        subCategoryRepository.deleteById(id);
    }
}