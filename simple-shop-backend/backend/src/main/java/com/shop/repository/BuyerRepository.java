package com.shop.repository;

import com.shop.model.Buyer;
import com.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    List<Buyer> findByProductOrderByCreatedAtDesc(Product product);
    List<Buyer> findAllByOrderByCreatedAtDesc();
}
