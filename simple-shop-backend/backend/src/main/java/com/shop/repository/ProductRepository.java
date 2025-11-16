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
    List<Product> findByIsActiveTrueOrderByCreatedAtDesc(); // 获取所有上架商品，按创建时间倒序
    List<Product> findBySubCategoryAndIsActiveTrue(SubCategory subCategory);
    List<Product> findBySubCategoryId(Long subCategoryId);
    List<Product> findBySubCategoryIdAndIsActiveTrue(Long subCategoryId); // 获取指定子分类的上架商品
    // 添加通过一级分类ID获取商品的方法
    List<Product> findBySubCategoryCategoryId(Long categoryId);
    List<Product> findBySubCategoryCategoryIdAndIsActiveTrue(Long categoryId); // 获取指定一级分类的上架商品
}