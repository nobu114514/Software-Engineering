package com.shop.repository;

import com.shop.model.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    // 检查用户是否已收藏某商品
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
    
    // 获取用户的所有收藏商品
    List<ProductFavorite> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    // 根据用户ID和商品ID查找收藏记录
    Optional<ProductFavorite> findByCustomerIdAndProductId(Long customerId, Long productId);
    
    // 删除用户的收藏商品
    void deleteByCustomerIdAndProductId(Long customerId, Long productId);
}