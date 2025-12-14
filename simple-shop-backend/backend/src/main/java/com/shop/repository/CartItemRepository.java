package com.shop.repository;

import com.shop.model.CartItem;
import com.shop.model.Customer;
import com.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.customer = :customer")
    List<CartItem> findByCustomer(@Param("customer") Customer customer);
    Optional<CartItem> findByCustomerAndProduct(Customer customer, Product product);
    void deleteByCustomer(Customer customer);
}