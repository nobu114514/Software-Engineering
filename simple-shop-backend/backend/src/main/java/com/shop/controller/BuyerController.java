package com.shop.controller;

import com.shop.model.Buyer;
import com.shop.service.BuyerService;
import com.shop.service.CustomerService;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyers")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CustomerService customerService;

    @PostMapping("/product/{productId}")
    public ResponseEntity<?> createBuyer(@RequestBody Buyer buyer, @PathVariable Long productId, @RequestHeader(value = "X-Username", required = false) String username) {
        // 验证用户是否已登录（简单实现，实际项目中应使用更安全的认证机制）
        if (username == null || username.isEmpty()) {
            // 返回未授权错误
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "请先登录再购买商品");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        try {
            // 验证必填字段
            if (buyer == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "购买信息不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (buyer.getName() == null || buyer.getName().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请填写联系人姓名");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (buyer.getPhone() == null || buyer.getPhone().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请填写联系电话");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (buyer.getAddress() == null || buyer.getAddress().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请填写收货地址");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // 创建购买意向 - 现在会抛出具体的异常
            Buyer created = buyerService.createBuyer(buyer, productId, username);
            
            // 清除循环引用，避免JSON序列化问题
            if (created.getProduct() != null) {
                created.getProduct().setSubCategory(null);
            }
            
            // 如果成功，返回创建的购买意向
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "购买意向创建成功");
            successResponse.put("data", created);
            return ResponseEntity.ok(successResponse);
        } catch (RuntimeException e) {
            // 捕获所有运行时异常，特别是BuyerServiceException
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            // 提取错误信息
            String errorMessage = e.getMessage();
            
            // 根据错误信息确定HTTP状态码
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (errorMessage.contains("不存在")) {
                status = HttpStatus.NOT_FOUND;
            } else if (errorMessage.contains("失败")) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            
            errorResponse.put("message", errorMessage);
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            // 捕获所有其他异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "系统异常，请稍后再试：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public List<Buyer> getAllBuyers() {
        List<Buyer> buyers = buyerService.getAllBuyers();
        // 清除循环引用，避免JSON序列化问题
        for (Buyer buyer : buyers) {
            if (buyer.getProduct() != null) {
                buyer.getProduct().setSubCategory(null);
            }
        }
        return buyers;
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Boolean> completeTransaction(
            @PathVariable Long id,
            @RequestParam boolean success) {
        boolean result = buyerService.completeTransaction(id, success);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
}
