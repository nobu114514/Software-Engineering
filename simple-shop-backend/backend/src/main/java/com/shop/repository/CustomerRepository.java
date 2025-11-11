package com.shop.repository;

import com.shop.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    boolean existsByUsername(String username);
    
    // 支持分页和搜索的查询方法
    @Query("SELECT c FROM Customer c WHERE (:keyword IS NULL OR c.username LIKE %:keyword% OR c.phone LIKE %:keyword%)")
    Page<Customer> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}