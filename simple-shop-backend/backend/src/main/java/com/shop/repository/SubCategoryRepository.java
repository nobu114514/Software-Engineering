package com.shop.repository;

import com.shop.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    List<SubCategory> findByCategoryIdAndActiveTrueOrderBySortOrderAsc(Long categoryId);
    List<SubCategory> findByCategoryIdOrderBySortOrderAsc(Long categoryId);
    List<SubCategory> findByActiveTrue();
    boolean existsByCategoryIdAndName(Long categoryId, String name);
    boolean existsByCategoryIdAndNameAndIdNot(Long categoryId, String name, Long id);
}