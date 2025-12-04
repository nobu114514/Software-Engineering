package com.shop.service.impl;

import com.shop.dto.SubCategoryDTO;
import com.shop.model.Category;
import com.shop.model.SubCategory;
import com.shop.repository.CategoryRepository;
import com.shop.repository.SubCategoryRepository;
import com.shop.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubCategoryServiceImpl implements SubCategoryService {

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    @Override
    public SubCategoryDTO createSubCategory(SubCategoryDTO subCategoryDTO) {
        // 获取分类
        Category category = categoryRepository.findById(subCategoryDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在: " + subCategoryDTO.getCategoryId()));

        // 检查分类下是否已存在同名二级分类
        if (subCategoryRepository.existsByCategoryIdAndName(category.getId(), subCategoryDTO.getName())) {
            throw new IllegalArgumentException("该分类下已存在同名二级分类");
        }

        SubCategory subCategory = new SubCategory();
        updateSubCategoryFromDTO(subCategory, subCategoryDTO, category);
        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        return convertToDTO(savedSubCategory);
    }

    @Override
    public SubCategoryDTO updateSubCategory(Long id, SubCategoryDTO subCategoryDTO) {
        // 获取二级分类
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("二级分类不存在: " + id));

        // 获取分类
        Category category = categoryRepository.findById(subCategoryDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在: " + subCategoryDTO.getCategoryId()));

        // 检查是否与其他同级二级分类重名
        if (!subCategory.getName().equals(subCategoryDTO.getName()) ||
                !subCategory.getCategory().getId().equals(category.getId())) {
            if (subCategoryRepository.existsByCategoryIdAndNameAndIdNot(
                    category.getId(), subCategoryDTO.getName(), id)) {
                throw new IllegalArgumentException("该分类下已存在同名二级分类");
            }
        }

        updateSubCategoryFromDTO(subCategory, subCategoryDTO, category);
        SubCategory updatedSubCategory = subCategoryRepository.save(subCategory);
        return convertToDTO(updatedSubCategory);
    }

    @Override
    public Optional<SubCategoryDTO> getSubCategoryDTOById(Long id) {
        return subCategoryRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<SubCategoryDTO> getAllSubCategoryDTOs() {
        List<SubCategory> subCategories = subCategoryRepository.findAll();
        return subCategories.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SubCategoryDTO> getSubCategoryDTOsByCategoryId(Long categoryId) {
        List<SubCategory> subCategories = subCategoryRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        return subCategories.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private SubCategoryDTO convertToDTO(SubCategory subCategory) {
        SubCategoryDTO dto = new SubCategoryDTO();
        dto.setId(subCategory.getId());
        dto.setName(subCategory.getName());
        dto.setDescription(subCategory.getDescription());
        dto.setActive(subCategory.isActive());
        dto.setSortOrder(subCategory.getSortOrder());
        dto.setIcon(subCategory.getIcon());
        dto.setCategoryId(subCategory.getCategory().getId());
        return dto;
    }

    private void updateSubCategoryFromDTO(SubCategory entity, SubCategoryDTO dto, Category category) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());
        entity.setSortOrder(dto.getSortOrder());
        entity.setIcon(dto.getIcon());
        entity.setCategory(category);
    }
}