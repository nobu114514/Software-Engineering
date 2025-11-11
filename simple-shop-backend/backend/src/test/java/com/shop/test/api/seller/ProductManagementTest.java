package com.shop.test.api.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 测试用例 S-002：发布新商品
    @Test
    public void testCreateProduct() throws Exception {
        Product product = new Product();
        product.setName("测试商品");
        product.setDescription("测试描述");
        product.setPrice(99.9);
        product.setImageUrl("http://test.com/image.jpg");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试商品"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // 测试用例 S-004：商品上架/下架
    @Test
    public void testActivateDeactivateProduct() throws Exception {
        // 先创建测试商品
        Product product = new Product();
        product.setName("上架测试商品");
        product.setPrice(199.0);
        String productJson = objectMapper.writeValueAsString(product);
        String createResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // 测试上架（激活）
        mockMvc.perform(put("/api/products/" + productId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // 测试下架（通过冻结实现）
        mockMvc.perform(put("/api/products/" + productId + "/freeze")
                        .param("freeze", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试用例 S-005：冻结/解冻商品
    @Test
    public void testFreezeProduct() throws Exception {
        // 创建测试商品
        Product product = new Product();
        product.setName("冻结测试商品");
        product.setPrice(99.0);
        String productJson = objectMapper.writeValueAsString(product);
        String createResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // 冻结商品
        mockMvc.perform(put("/api/products/" + productId + "/freeze")
                        .param("freeze", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 验证冻结状态
        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frozen").value(true));

        // 解冻商品
        mockMvc.perform(put("/api/products/" + productId + "/freeze")
                        .param("freeze", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}