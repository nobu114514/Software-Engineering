package com.shop.service;

import com.shop.model.ProductFavorite;
import java.util.List;

public interface ProductFavoriteService {
    // 添加商品到收藏
    ProductFavorite addToFavorites(Long customerId, Long productId);
    
    // 从收藏中移除商品
    void removeFromFavorites(Long customerId, Long productId);
    
    // 检查商品是否已被收藏
    boolean isProductFavorited(Long customerId, Long productId);
    
    // 获取用户的所有收藏商品
    List<ProductFavorite> getCustomerFavorites(Long customerId);
}