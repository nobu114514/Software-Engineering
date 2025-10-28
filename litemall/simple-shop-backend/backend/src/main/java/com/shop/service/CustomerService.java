package com.shop.service;

import com.shop.model.Customer;
import com.shop.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean register(String username, String password, String phone, String defaultLocation) {
        // 检查用户名是否已存在
        if (customerRepository.existsByUsername(username)) {
            return false;
        }
        
        // 创建新客户
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(password); // 实际应用中应加密存储
        customer.setPhone(phone);
        customer.setDefaultLocation(defaultLocation);
        
        customerRepository.save(customer);
        return true;
    }

    public Optional<Customer> login(String username, String password) {
        return customerRepository.findByUsername(username)
                .filter(customer -> customer.getPassword().equals(password));
    }
    
    // 根据用户名查找客户
    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }
    
    // 根据ID查找客户
    public Optional<Customer> findById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    // 验证手机号格式（简单验证，11位数字）
    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    // 验证密码长度
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    // 获取所有客户列表
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    // 获取指定客户的订单记录（包括购买意向）
    public List<Map<String, Object>> getCustomerOrders(Long customerId) {
        // 检查客户是否存在
        if (!customerRepository.existsById(customerId)) {
            return Collections.emptyList();
        }
        
        // 使用原生SQL查询订单数据和购买意向
        String sql = "SELECT " +
                     "CASE " +
                     "WHEN o.id IS NOT NULL THEN 'order' " +
                     "WHEN b.id IS NOT NULL THEN 'buyer' " +
                     "END as type, " +
                     "COALESCE(o.id, b.id) as id, " +
                     "COALESCE(p1.name, p2.name) as product_name, " +
                     "o.price, " +
                     "o.quantity, " +
                     "COALESCE(o.created_at, b.created_at) as created_at, " +
                     "COALESCE(o.status, CASE WHEN b.is_completed THEN 'completed' ELSE 'pending' END) as status " +
                     "FROM customers c " +
                     "LEFT JOIN orders o ON c.id = o.customer_id " +
                     "LEFT JOIN product p1 ON o.product_id = p1.id " +
                     "LEFT JOIN buyers b ON c.id = b.customer_id " +
                     "LEFT JOIN product p2 ON b.product_id = p2.id " +
                     "WHERE c.id = ? AND (o.id IS NOT NULL OR b.id IS NOT NULL) " +
                     "ORDER BY created_at DESC";
        
        return jdbcTemplate.query(sql, new Object[]{customerId}, (rs, rowNum) -> {
            Map<String, Object> order = new HashMap<>();
            order.put("type", rs.getString("type"));
            order.put("id", rs.getLong("id"));
            order.put("product_name", rs.getString("product_name"));
            order.put("price", rs.getBigDecimal("price"));
            order.put("quantity", rs.getInt("quantity"));
            order.put("created_at", rs.getTimestamp("created_at"));
            order.put("status", rs.getString("status"));
            return order;
        });
    }
}