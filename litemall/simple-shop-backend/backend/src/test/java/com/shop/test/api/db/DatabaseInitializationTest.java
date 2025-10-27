package com.shop.test.api.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DatabaseInitializationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 测试用例 DB-002：数据表创建验证
    @Test
    public void testTablesCreated() {
        // 验证product表存在
        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_NAME = 'product'",
                Integer.class
        );
        assertEquals(1, productCount);

        // 验证purchase_intention表存在
        Integer intentionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_NAME = 'purchase_intention'",
                Integer.class
        );
        assertEquals(1, intentionCount);

        // 验证seller表存在
        Integer sellerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_NAME = 'seller'",
                Integer.class
        );
        assertEquals(1, sellerCount);
    }

    // 测试用例 DB-003：初始卖家数据验证
    @Test
    public void testInitialSellerData() {
        String password = jdbcTemplate.queryForObject(
                "SELECT password FROM seller WHERE username = 'seller'",
                String.class
        );
        assertEquals("123", password);
    }

    // 测试用例 DB-004：数据库连接验证（通过上下文加载间接验证）
    @Test
    public void testDatabaseConnection() {
        // 若能执行简单查询则说明连接正常
        Integer count = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, count);
    }
}