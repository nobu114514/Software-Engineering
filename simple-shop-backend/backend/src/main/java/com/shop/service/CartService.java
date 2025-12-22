package com.shop.service;

import com.shop.model.CartItem;
import com.shop.model.Customer;
import com.shop.model.Product;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CustomerRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    // 获取当前登录用户
    private Customer getCurrentCustomer(String username) {
        // 根据用户名获取当前登录用户
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 添加商品到购物车
    public int addToCart(String username, Long productId, int quantity) {
        Customer customer = getCurrentCustomer(username);
        Optional<Product> productOptional = productRepository.findById(productId);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            // 检查商品是否活跃
            if (!product.isActive()) {
                throw new RuntimeException("商品已下架");
            }
            
            // 检查购物车中是否已存在该商品
            Optional<CartItem> existingItemOptional = cartItemRepository.findByCustomerAndProduct(customer, product);
            
            if (existingItemOptional.isPresent()) {
                // 商品已存在，增加数量
                CartItem existingItem = existingItemOptional.get();
                existingItem.increaseQuantity(quantity);
                cartItemRepository.save(existingItem);
            } else {
                // 商品不存在，添加新项
                CartItem newItem = new CartItem(customer, product, quantity);
                cartItemRepository.save(newItem);
            }
            
            return getCartItems(username).size(); // 返回购物车中商品项的数量
        } else {
            throw new RuntimeException("商品不存在");
        }
    }

    // 获取购物车中商品的总数量
    public int getTotalQuantity(String username) {
        Customer customer = getCurrentCustomer(username);
        List<CartItem> cart = cartItemRepository.findByCustomer(customer);
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // 获取购物车内容
    public List<CartItem> getCartItems(String username) {
        Customer customer = getCurrentCustomer(username);
        System.out.println("Current customer: " + customer.getId() + " - " + customer.getUsername());
        List<CartItem> cartItems = cartItemRepository.findByCustomer(customer);
        System.out.println("Cart items found: " + cartItems.size());
        
        // 处理购物车项，确保只返回前端需要的信息
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            Customer cartItemCustomer = item.getCustomer();
            
            if (product != null) {
                System.out.println("Item: " + item.getId() + ", Product: " + product.getId() + " - " + product.getName() + ", Quantity: " + item.getQuantity());
                
                // 设置 transient 字段
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setImageUrl(product.getImageUrl());
                item.setPrice(product.getPrice());
            }
            
            // 将关联对象设置为 null，避免 JSON 序列化时的无限循环引用
            item.setProduct(null);
            item.setCustomer(null);
        }
        return cartItems;
    }

    // 更新购物车中商品的数量
    public boolean updateQuantity(String username, Long productId, int quantity) {
        Customer customer = getCurrentCustomer(username);
        Optional<Product> productOptional = productRepository.findById(productId);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            Optional<CartItem> existingItemOptional = cartItemRepository.findByCustomerAndProduct(customer, product);
            
            if (existingItemOptional.isPresent()) {
                CartItem existingItem = existingItemOptional.get();
                if (quantity <= 0) {
                    // 数量为0或负数，移除商品
                    cartItemRepository.delete(existingItem);
                } else {
                    // 检查库存
                    if (quantity > product.getStock()) {
                        throw new RuntimeException("购买数量超过库存限制");
                    }
                    // 更新数量
                    existingItem.setQuantity(quantity);
                    cartItemRepository.save(existingItem);
                }
                return true;
            }
        }
        return false;
    }

    // 从购物车中移除商品
    public boolean removeFromCart(String username, Long productId) {
        Customer customer = getCurrentCustomer(username);
        Optional<Product> productOptional = productRepository.findById(productId);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            Optional<CartItem> existingItemOptional = cartItemRepository.findByCustomerAndProduct(customer, product);
            
            existingItemOptional.ifPresent(cartItemRepository::delete);
            return existingItemOptional.isPresent();
        }
        return false;
    }

    // 清空购物车
    public void clearCart(String username) {
        Customer customer = getCurrentCustomer(username);
        cartItemRepository.deleteByCustomer(customer);
    }
    
    // 批量从购物车中移除商品
    public void removeItemsFromCart(String username, List<Long> productIds) {
        Customer customer = getCurrentCustomer(username);
        
        for (Long productId : productIds) {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                Optional<CartItem> cartItemOptional = cartItemRepository.findByCustomerAndProduct(customer, product);
                cartItemOptional.ifPresent(cartItemRepository::delete);
            }
        }
    }
}