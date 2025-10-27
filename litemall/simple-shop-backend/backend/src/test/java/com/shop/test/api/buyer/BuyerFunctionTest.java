package com.shop.test.api.buyer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.model.Buyer;
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
public class BuyerFunctionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 测试用例 B-001：查看商品
    @Test
    public void testViewProduct() throws Exception {
        // 先创建一个在售商品
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"买家测试商品\",\"price\":88.0}"));

        // 验证买家页面能看到商品
        mockMvc.perform(get("/api/products/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("买家测试商品"));
    }

    // 测试用例 B-002：提交购买意向
    @Test
    public void testSubmitPurchaseIntention() throws Exception {
        // 创建测试商品
        String productResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"购买测试商品\",\"price\":99.0}"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long productId = objectMapper.readTree(productResponse).get("id").asLong();

        // 提交购买意向
        Buyer buyer = new Buyer();
        buyer.setName("测试买家");
        buyer.setPhone("13900139000");
        buyer.setAddress("测试地址");

        mockMvc.perform(post("/api/buyers/product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试买家"));

        // 验证商品状态变为待处理（冻结）
        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frozen").value(true));
    }
}