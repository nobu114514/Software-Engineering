package com.shop.controller;

import com.shop.model.ProductFavorite;
import com.shop.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/favorites")
public class ProductFavoriteController {
    
    @Autowired
    private ProductFavoriteService favoriteService;
    
    // 添加收藏
    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @PathVariable Long productId,
            @RequestParam Long customerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var favorite = favoriteService.addToFavorites(customerId, productId);
            // 清除循环引用
            favorite.getCustomer().setFavoriteProducts(null);
            favorite.getProduct().setSubCategory(null);
            response.put("success", true);
            response.put("data", favorite);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 取消收藏
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @PathVariable Long productId,
            @RequestParam Long customerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            favoriteService.removeFromFavorites(customerId, productId);
            response.put("success", true);
            response.put("message", "Product removed from favorites");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 检查是否已收藏
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @PathVariable Long productId,
            @RequestParam Long customerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isFavorited = favoriteService.isProductFavorited(customerId, productId);
            response.put("success", true);
            response.put("isFavorited", isFavorited);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 获取用户收藏列表
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getFavorites(@RequestParam Long customerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ProductFavorite> favorites = favoriteService.getCustomerFavorites(customerId);
            // 清除循环引用
            for (var favorite : favorites) {
                favorite.getCustomer().setFavoriteProducts(null);
                if (favorite.getProduct().getSubCategory() != null) {
                    favorite.getProduct().getSubCategory().setCategory(null);
                }
            }
            response.put("success", true);
            response.put("data", favorites);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}