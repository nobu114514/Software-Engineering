package com.shop.controller;

import com.shop.model.Order;
import com.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单（批量下单）
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request, @RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }

            // 从请求体中获取商品ID列表
            List<?> productIdList = (List<?>) request.get("productIds");
            if (productIdList == null || productIdList.isEmpty()) {
                throw new RuntimeException("请选择要购买的商品");
            }
            
            // 转换为Long类型列表
            List<Long> productIds = new ArrayList<>();
            for (Object id : productIdList) {
                if (id instanceof Number) {
                    productIds.add(((Number) id).longValue());
                } else if (id instanceof String) {
                    productIds.add(Long.parseLong((String) id));
                } else {
                    throw new RuntimeException("无效的商品ID类型");
                }
            }
            System.out.println("转换后的商品ID列表: " + productIds);

            // 调用Service创建订单
            Order order = orderService.createOrder(username, productIds);

            // 构造响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("orderId", order.getId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("orderStatus", order.getOrderStatus());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 构造错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 获取用户的所有订单
    @GetMapping("/user")
    public ResponseEntity<?> getOrders(@RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }

            List<Order> orders = orderService.getOrdersByUsername(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 获取订单详情
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId, @RequestHeader(value = "X-Username", required = false) String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("请先登录");
            }

            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));

            // 验证订单是否属于当前用户
            if (!order.getCustomer().getUsername().equals(username)) {
                throw new RuntimeException("无权查看该订单");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", order);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
