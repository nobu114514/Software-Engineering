package com.shop.controller;

import com.shop.model.Seller;
import com.shop.service.SellerService;
import com.shop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // 初始化卖家账号
    @PostConstruct
    public void init() {
        sellerService.initializeSeller();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username, 
            @RequestParam String password) {
        Optional<Seller> seller = sellerService.login(username, password);
        Map<String, Object> response = new HashMap<>();
        
        if (seller.isPresent()) {
            String token = jwtUtil.generateToken(username, "seller");
            response.put("success", true);
            response.put("token", token);
            response.put("username", username);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(
            @RequestParam String username,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        boolean success = sellerService.changePassword(username, oldPassword, newPassword);
        return ResponseEntity.ok(success);
    }
}
