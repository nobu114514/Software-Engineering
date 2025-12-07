package com.shop.service.impl;

import com.shop.model.ProductFavorite;
import com.shop.repository.ProductFavoriteRepository;
import com.shop.repository.CustomerRepository;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductFavoriteServiceImpl implements ProductFavoriteService {
    
    @Autowired
    private ProductFavoriteRepository favoriteRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    @Transactional
    public ProductFavorite addToFavorites(Long customerId, Long productId) {
        // 检查用户和商品是否存在
        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        var product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 检查是否已收藏
        if (favoriteRepository.existsByCustomerIdAndProductId(customerId, productId)) {
            throw new RuntimeException("Product already favorited");
        }
        
        // 创建收藏记录
        var favorite = new ProductFavorite();
        favorite.setCustomer(customer);
        favorite.setProduct(product);
        
        return favoriteRepository.save(favorite);
    }
    
    @Override
    @Transactional
    public void removeFromFavorites(Long customerId, Long productId) {
        favoriteRepository.deleteByCustomerIdAndProductId(customerId, productId);
    }
    
    @Override
    public boolean isProductFavorited(Long customerId, Long productId) {
        return favoriteRepository.existsByCustomerIdAndProductId(customerId, productId);
    }
    
    @Override
    public List<ProductFavorite> getCustomerFavorites(Long customerId) {
        return favoriteRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}