package com.shop.controller;

import com.shop.model.Seller;
import com.shop.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    // 初始化卖家账号
    @PostConstruct
    public void init() {
        sellerService.initializeSeller();
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(
            @RequestParam String username, 
            @RequestParam String password) {
        boolean success = sellerService.login(username, password).isPresent();
        return ResponseEntity.ok(success);
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
