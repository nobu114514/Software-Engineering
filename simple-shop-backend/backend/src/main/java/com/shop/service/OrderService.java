package com.shop.service;

import com.shop.model.*;
import com.shop.repository.*;
import com.shop.service.StockLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private StockLogService stockLogService;

    // 获取当前登录用户
    private Customer getCurrentCustomer(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 批量下单功能
    @Transactional
    public Order createOrder(String username, List<Long> productIds) {
        // 1. 获取当前用户
        Customer customer = getCurrentCustomer(username);
        System.out.println("当前用户: " + username + "，用户ID: " + customer.getId());
        System.out.println("选中的商品ID列表: " + productIds);

        // 2. 获取购物车中选中的商品项
        List<CartItem> selectedCartItems = new ArrayList<>();
        double totalAmount = 0.0;

        // 3. 验证库存并计算总金额
        for (Long productId : productIds) {
            // 查找购物车中的商品项
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                // 查找购物车中该用户的该商品
                Optional<CartItem> cartItemOptional = cartItemRepository.findByCustomerAndProduct(customer, product);
                
                if (cartItemOptional.isPresent()) {
                    CartItem cartItem = cartItemOptional.get();
                    selectedCartItems.add(cartItem);

                    // 验证库存
                    if (cartItem.getQuantity() > product.getStock()) {
                        throw new RuntimeException("商品 '" + product.getName() + "' 库存不足");
                    }

                    // 计算总金额
                    totalAmount += product.getPrice() * cartItem.getQuantity();
                } else {
                    throw new RuntimeException("购物车中不存在该商品: " + product.getName());
                }
            } else {
                throw new RuntimeException("商品不存在: ID=" + productId);
            }
        }

        // 4. 创建订单
        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(Order.OrderStatus.下单); // 设置状态为"下单"

        // 5. 创建订单商品项
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : selectedCartItems) {
            Long productId = cartItem.getProductId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在: ID=" + productId));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice()); // 记录下单时的价格

            orderItems.add(orderItem);

            // 6. 减少商品库存
            int currentStock = product.getStock();
            int quantity = cartItem.getQuantity();
            product.setStock(currentStock - quantity);
            productRepository.save(product);
            
            // 记录库存日志
            int newStock = currentStock - quantity;
            stockLogService.createStockLog(product, -quantity, currentStock, newStock, "下单成功", "下单成功，库存减少" + quantity + "个单位");
        }

        order.setOrderItems(orderItems);

        // 7. 保存订单
        Order savedOrder = orderRepository.save(order);

        // 8. 从购物车中移除已下单的商品
        for (CartItem cartItem : selectedCartItems) {
            cartItemRepository.delete(cartItem);
        }

        return savedOrder;
    }

    // 获取用户的所有订单
    public List<Order> getOrdersByUsername(String username) {
        Customer customer = getCurrentCustomer(username);
        return orderRepository.findByCustomer(customer);
    }

    // 获取订单详情
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
