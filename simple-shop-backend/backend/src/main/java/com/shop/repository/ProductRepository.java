package com.shop.repository;

import com.shop.model.Product;
import com.shop.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIsActiveTrue();
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findBySubCategoryAndIsActiveTrue(SubCategory subCategory);
    List<Product> findBySubCategoryId(Long subCategoryId);
    // 添加通过一级分类ID获取商品的方法
    List<Product> findBySubCategoryCategoryId(Long categoryId);
}