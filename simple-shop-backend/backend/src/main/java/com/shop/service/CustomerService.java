package com.shop.service;

import com.shop.model.Customer;
import com.shop.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    
    // 获取所有客户列表（兼容原有接口）
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    // 获取全部客户列表（支持分页，不进行搜索）
    public Page<Customer> getAllCustomersWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByKeyword(null, pageable);
    }
    
    // 获取客户列表（支持分页和搜索）
    public Page<Customer> getCustomers(String keyword, int page, int size) {
        System.out.println("Service received keyword: '" + keyword + "'");
        Pageable pageable = PageRequest.of(page, size);
        // 对搜索关键词进行trim处理
        String searchKeyword = keyword != null ? keyword.trim() : null;
        System.out.println("Service using searchKeyword: '" + searchKeyword + "'");
        return customerRepository.findByKeyword(searchKeyword, pageable);
    }
    
    // 获取指定客户的订单记录（从buyers表中）
    public List<Map<String, Object>> getCustomerOrders(Long customerId, String status, String sort) {
        // 构建SQL查询，支持状态筛选和排序
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ")
                 .append("b.id as id, ")
                 .append("b.address as address, ")
                 .append("b.created_at as created_at, ")
                 .append("b.is_completed as is_completed, ")
                 .append("b.order_status as order_status, ")
                 .append("b.name as buyer_name, ")
                 .append("b.notes as notes, ")
                 .append("b.phone as phone, ")
                 .append("p.name as product_name, ")
                 .append("p.price as price, ")
                 .append("CASE b.order_status ")
                 .append("    WHEN 0 THEN '客户下单' ")
                 .append("    WHEN 1 THEN '商家确认' ")
                 .append("    WHEN 2 THEN '备货完成' ")
                 .append("    WHEN 3 THEN '开始发货' ")
                 .append("    WHEN 4 THEN '交易完成' ")
                 .append("    WHEN 5 THEN '交易失败' ")
                 .append("    ELSE '未知状态' ")
                 .append("END as status ")
                 .append("FROM buyers b ")
                 .append("LEFT JOIN products p ON b.product_id = p.id ")
                 .append("WHERE b.customer_id = ? ");
        
        // 添加状态筛选条件
        List<Object> paramsList = new ArrayList<>();
        paramsList.add(customerId);
        
        if (status != null && !status.isEmpty()) {
            // 支持新的状态筛选
            try {
                // 首先尝试将status转换为数字
                int statusCode = Integer.parseInt(status);
                if (statusCode >= 0 && statusCode <= 5) {
                    sqlBuilder.append("AND b.order_status = ? ");
                    paramsList.add(statusCode);
                }
            } catch (NumberFormatException e) {
                // 如果不是数字，则尝试匹配状态名称或旧状态值
                switch (status) {
                    case "客户下单":
                        sqlBuilder.append("AND b.order_status = 0 ");
                        break;
                    case "商家确认":
                        sqlBuilder.append("AND b.order_status = 1 ");
                        break;
                    case "备货完成":
                        sqlBuilder.append("AND b.order_status = 2 ");
                        break;
                    case "开始发货":
                        sqlBuilder.append("AND b.order_status = 3 ");
                        break;
                    case "交易完成":
                        sqlBuilder.append("AND b.order_status = 4 ");
                        break;
                    case "交易失败":
                        sqlBuilder.append("AND b.order_status = 5 ");
                        break;
                    // 兼容旧的状态值
                    case "completed":
                        sqlBuilder.append("AND b.order_status >= 1 ");
                        break;
                    case "pending":
                        sqlBuilder.append("AND b.order_status = 0 ");
                        break;
                    case "failed":
                        sqlBuilder.append("AND b.order_status = 5 ");
                        break;
                }
            }
        }
        
        // 添加排序条件
        String orderDirection = "desc".equalsIgnoreCase(sort) ? "DESC" : "ASC";
        sqlBuilder.append("ORDER BY b.created_at ").append(orderDirection);
        
        Object[] params = paramsList.toArray();
        
        return jdbcTemplate.query(sqlBuilder.toString(), params, (rs, rowNum) -> {
            Map<String, Object> order = new HashMap<>();
            order.put("id", rs.getLong("id"));
            order.put("address", rs.getString("address"));
            order.put("created_at", rs.getTimestamp("created_at"));
            order.put("is_completed", rs.getBoolean("is_completed"));
            order.put("order_status", rs.getInt("order_status"));
            order.put("buyer_name", rs.getString("buyer_name"));
            order.put("notes", rs.getString("notes"));
            order.put("phone", rs.getString("phone"));
            order.put("product_name", rs.getString("product_name"));
            order.put("price", rs.getBigDecimal("price"));
            order.put("status", rs.getString("status"));
            return order;
        });
    }
}