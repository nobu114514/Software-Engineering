package com.shop.repository;

import com.shop.model.Buyer;
import com.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    List<Buyer> findByProductOrderByCreatedAtDesc(Product product);
    List<Buyer> findAllByOrderByCreatedAtDesc();
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Buyer b WHERE b.product.id = ?1")
    void deleteByProductId(Long productId);
}
