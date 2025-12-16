package com.shop.controller;

import com.shop.model.CartItem;
import com.shop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 添加商品到购物车
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request, @RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }
            
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = request.containsKey("quantity") ? Integer.valueOf(request.get("quantity").toString()) : 1;
            
            int cartSize = cartService.addToCart(username, productId, quantity);
            int totalQuantity = cartService.getTotalQuantity(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartSize", cartSize); // 购物车中商品项的数量
            response.put("totalQuantity", totalQuantity); // 购物车中商品的总数量
            response.put("message", "商品添加成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 获取购物车内容
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(@RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }
            
            List<CartItem> cartItems = cartService.getCartItems(username);
            int totalQuantity = cartService.getTotalQuantity(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartItems", cartItems);
            response.put("totalQuantity", totalQuantity);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 更新购物车中商品的数量
    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody Map<String, Object> request, @RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }
            
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.valueOf(request.get("quantity").toString());
            
            boolean success = cartService.updateQuantity(username, productId, quantity);
            List<CartItem> cartItems = cartService.getCartItems(username);
            int totalQuantity = cartService.getTotalQuantity(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("cartItems", cartItems);
            response.put("totalQuantity", totalQuantity);
            response.put("message", success ? "数量更新成功" : "更新失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 从购物车中移除商品
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, @RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }
            
            boolean success = cartService.removeFromCart(username, productId);
            List<CartItem> cartItems = cartService.getCartItems(username);
            int totalQuantity = cartService.getTotalQuantity(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("cartItems", cartItems);
            response.put("totalQuantity", totalQuantity);
            response.put("message", success ? "商品移除成功" : "移除失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 清空购物车
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }
            
            cartService.clearCart(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartItems", null);
            response.put("totalQuantity", 0);
            response.put("message", "购物车已清空");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 获取购物车统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getCartStats(@RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }
            
            List<CartItem> cartItems = cartService.getCartItems(username);
            int totalItems = cartItems.size();
            int totalQuantity = cartService.getTotalQuantity(username);
            double totalAmount = cartItems.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalItems", totalItems);
            response.put("totalQuantity", totalQuantity);
            response.put("totalAmount", totalAmount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}