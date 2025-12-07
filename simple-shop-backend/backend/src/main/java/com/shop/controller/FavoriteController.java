package com.shop.controller;

import com.shop.model.Favorite;
import com.shop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    // 获取用户的所有收藏
    @GetMapping("/{username}")
    public ResponseEntity<List<Map<String, Object>>> getUserFavorites(@PathVariable String username) {
        List<Map<String, Object>> favorites = favoriteService.getUserFavorites(username);
        return ResponseEntity.ok(favorites);
    }

    // 添加收藏
    @PostMapping("/{username}/{productId}")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @PathVariable String username,
            @PathVariable Long productId) {
        try {
            Favorite favorite = favoriteService.addFavorite(username, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "收藏成功");

            // 创建不包含循环引用的响应数据
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("id", favorite.getId());
            favoriteData.put("username", favorite.getUsername());
            favoriteData.put("createdAt", favorite.getCreatedAt());

            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("id", favorite.getProduct().getId());
            productInfo.put("name", favorite.getProduct().getName());
            productInfo.put("price", favorite.getProduct().getPrice());
            productInfo.put("imageUrl", favorite.getProduct().getImageUrl());
            productInfo.put("stock", favorite.getProduct().getStock());
            productInfo.put("active", favorite.getProduct().isActive());

            favoriteData.put("product", productInfo);
            response.put("data", favoriteData);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 取消收藏
    @DeleteMapping("/{username}/{productId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable String username,
            @PathVariable Long productId) {
        try {
            favoriteService.removeFavorite(username, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "取消收藏成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 检查是否收藏
    @GetMapping("/{username}/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable String username,
            @PathVariable Long productId) {
        boolean isFavorited = favoriteService.isFavorited(username, productId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);
        return ResponseEntity.ok(response);
    }
}