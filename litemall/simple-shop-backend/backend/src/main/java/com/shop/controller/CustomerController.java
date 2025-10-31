package com.shop.controller;

import com.shop.model.Customer;
import com.shop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String defaultLocation) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 验证手机号格式
        if (!customerService.isValidPhone(phone)) {
            response.put("success", false);
            response.put("message", "手机号格式不正确，请输入11位有效的手机号码");
            return ResponseEntity.ok(response);
        }
        
        // 验证密码长度
        if (!customerService.isValidPassword(password)) {
            response.put("success", false);
            response.put("message", "密码长度不能少于6位");
            return ResponseEntity.ok(response);
        }
        
        // 执行注册
        boolean success = customerService.register(username, password, phone, defaultLocation);
        
        if (success) {
            response.put("success", true);
            response.put("message", "注册成功");
        } else {
            response.put("success", false);
            response.put("message", "用户名已存在");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = customerService.login(username, password).isPresent();
        
        if (success) {
            response.put("success", true);
            response.put("message", "登录成功");
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        
        return ResponseEntity.ok(response);
    }

    // 获取所有客户列表（仅卖家可访问）
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // 获取当前登录用户的订单记录（根据请求头中的用户名）
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "desc") String sort) {
        // 验证用户是否已登录
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "请先登录再查看订单");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // 根据用户名查找客户
        Optional<Customer> customerOpt = customerService.findByUsername(username);
        if (!customerOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // 获取客户ID并查询订单
        Long customerId = customerOpt.get().getId();
        List<Map<String, Object>> orders = customerService.getCustomerOrders(customerId, status, sort);
        return ResponseEntity.ok(orders);
    }

    // 获取指定客户的订单记录（仅卖家可访问）
    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<List<Map<String, Object>>> getCustomerOrders(@PathVariable Long customerId) {
        // 调用service方法，使用默认的筛选参数（不筛选状态，按时间降序）
        List<Map<String, Object>> orders = customerService.getCustomerOrders(customerId, null, "desc");
        return ResponseEntity.ok(orders);
    }
}