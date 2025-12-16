package com.shop.test.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.model.Product;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 测试用例 ENT-001：isFrozen 字段 JSON 序列化验证
    @Test
    public void testIsFrozenSerialization() throws JsonProcessingException {
        // 输入/前置条件：创建Product对象并设置isFrozen=true
        Product product = new Product();
        product.setId(1L);
        product.setFrozen(true); // 对应实体类isFrozen字段

        // 执行序列化
        String json = objectMapper.writeValueAsString(product);

        // 预期结果：JSON中字段为"frozen": true
        assertTrue(json.contains("\"frozen\":true"),
                "序列化后应包含\"frozen\":true");
        assertFalse(json.contains("\"isFrozen\":"),
                "序列化不应包含\"isFrozen\"字段");
    }

    // 测试用例 ENT-002：其他核心字段序列化验证
    @Test
    public void testCoreFieldsSerialization() throws JsonProcessingException {
        // 输入/前置条件：设置核心字段
        Product product = new Product();
        product.setName("测试商品");
        product.setPrice(99.0);
        product.setActive(true);

        // 执行序列化
        String json = objectMapper.writeValueAsString(product);

        // 预期结果：JSON包含对应字段
        assertTrue(json.contains("\"name\":\"测试商品\""), "名称字段序列化错误");
        assertTrue(json.contains("\"price\":99.0"), "价格字段序列化错误");
        assertTrue(json.contains("\"active\":true"), "active字段序列化错误");
    }
}