package com.shop.service;

import com.shop.model.Favorite;
import com.shop.model.Product;
import com.shop.repository.FavoriteRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    // 添加收藏
    @Transactional
    public Favorite addFavorite(String username, Long productId) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 检查是否已经收藏
        Optional<Favorite> existing = favoriteRepository.findByUsernameAndProductId(username, productId);
        if (existing.isPresent()) {
            throw new RuntimeException("商品已经在收藏列表中");
        }

        // 创建新收藏
        Favorite favorite = new Favorite(username, product);
        return favoriteRepository.save(favorite);
    }

    // 取消收藏
    @Transactional
    public void removeFavorite(String username, Long productId) {
        // 检查收藏是否存在
        Optional<Favorite> existing = favoriteRepository.findByUsernameAndProductId(username, productId);
        if (existing.isEmpty()) {
            throw new RuntimeException("收藏不存在");
        }

        favoriteRepository.deleteByUsernameAndProductId(username, productId);
    }

    // 检查商品是否被收藏
    public boolean isFavorited(String username, Long productId) {
        return favoriteRepository.findByUsernameAndProductId(username, productId).isPresent();
    }

    // 获取用户的所有收藏
    public List<Map<String, Object>> getUserFavorites(String username) {
        List<Favorite> favorites = favoriteRepository.findByUsername(username);
        return favorites.stream().map(favorite -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", favorite.getId());
            result.put("username", favorite.getUsername());
            result.put("createdAt", favorite.getCreatedAt());

            // 只返回商品的必要信息，避免循环引用
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("id", favorite.getProduct().getId());
            productInfo.put("name", favorite.getProduct().getName());
            productInfo.put("price", favorite.getProduct().getPrice());
            productInfo.put("imageUrl", favorite.getProduct().getImageUrl());
            productInfo.put("stock", favorite.getProduct().getStock());
            productInfo.put("active", favorite.getProduct().isActive());

            result.put("product", productInfo);
            return result;
        }).collect(Collectors.toList());
    }
}