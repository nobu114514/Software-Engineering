package com.shop.repository;

import com.shop.model.StockLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StockLogRepository extends JpaRepository<StockLog, Long> {
    // 根据商品ID获取库存日志
    List<StockLog> findByProductId(Long productId);
    
    // 根据商品ID按时间倒序获取库存日志，并关联查询商品信息
    @Query("SELECT sl FROM StockLog sl JOIN FETCH sl.product WHERE sl.product.id = :productId ORDER BY sl.createdAt DESC")
    List<StockLog> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);
    
    // 按时间倒序获取所有库存日志，并关联查询商品信息
    @Query("SELECT sl FROM StockLog sl JOIN FETCH sl.product ORDER BY sl.createdAt DESC")
    List<StockLog> findAllByOrderByCreatedAtDesc();
    
    // 根据商品名称搜索库存日志，并按时间倒序排列
    @Query("SELECT sl FROM StockLog sl JOIN FETCH sl.product WHERE LOWER(sl.product.name) LIKE LOWER(CONCAT('%', :productName, '%')) ORDER BY sl.createdAt DESC")
    List<StockLog> findByProductNameContainingOrderByCreatedAtDesc(@Param("productName") String productName);
    
    // 根据商品ID删除库存日志
    @Modifying
    @Transactional
    @Query("DELETE FROM StockLog sl WHERE sl.product.id = ?1")
    void deleteByProductId(Long productId);
}