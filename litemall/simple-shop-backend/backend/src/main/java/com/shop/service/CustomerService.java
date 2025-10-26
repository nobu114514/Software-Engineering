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
    
    // 获取指定客户的订单记录
    public List<Map<String, Object>> getCustomerOrders(Long customerId) {
        // 检查客户是否存在
        if (!customerRepository.existsById(customerId)) {
            return Collections.emptyList();
        }
        
        // 使用原生SQL查询订单数据
        String sql = "SELECT o.id, p.name as product_name, o.price, o.quantity, o.created_at, o.status " +
                     "FROM orders o " +
                     "LEFT JOIN product p ON o.product_id = p.id " +
                     "WHERE o.customer_id = ? " +
                     "ORDER BY o.created_at DESC";
        
        return jdbcTemplate.query(sql, new Object[]{customerId}, (rs, rowNum) -> {
            Map<String, Object> order = new HashMap<>();
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