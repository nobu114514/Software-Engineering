package com.shop.repository;

import com.shop.model.Product;
import com.shop.model.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 分页查询方法
    Page<Product> findByIsActiveTrue(Pageable pageable); // 分页查询所有上架商品
    Page<Product> findBySubCategoryCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable); // 分页查询指定一级分类的上架商品
    Page<Product> findBySubCategoryIdAndIsActiveTrue(Long subCategoryId, Pageable pageable); // 分页查询指定子分类的上架商品

    // 搜索相关方法
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchActiveProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "p.subCategory.category.id = :categoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchActiveProductsByCategory(@Param("categoryId") Long categoryId, 
                                                 @Param("keyword") String keyword, 
                                                 Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "p.subCategory.id = :subCategoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchActiveProductsBySubCategory(@Param("subCategoryId") Long subCategoryId, 
                                                    @Param("keyword") String keyword, 
                                                    Pageable pageable);
}