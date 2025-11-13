package com.shop.service;

import com.shop.dto.SubCategoryDTO;
import com.shop.model.SubCategory;

import java.util.List;
import java.util.Optional;

public interface SubCategoryService {

    List<SubCategory> getActiveSubCategoriesByCategoryId(Long categoryId);

    List<SubCategory> getAllActiveSubCategories();

    Optional<SubCategory> getSubCategoryById(Long id);

    SubCategory createSubCategory(SubCategory subCategory);

    SubCategory updateSubCategory(SubCategory subCategory);

    void deleteSubCategory(Long id);
    
    // DTO方法
    SubCategoryDTO createSubCategory(SubCategoryDTO subCategoryDTO);
    
    SubCategoryDTO updateSubCategory(Long id, SubCategoryDTO subCategoryDTO);
    
    Optional<SubCategoryDTO> getSubCategoryDTOById(Long id);
    
    List<SubCategoryDTO> getAllSubCategoryDTOs();
    
    List<SubCategoryDTO> getSubCategoryDTOsByCategoryId(Long categoryId);
}