package com.shop.test.api.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.model.Buyer;
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
public class PurchaseIntentionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 测试用例 S-006：查看购买意向
    @Test
    public void testViewPurchaseIntentions() throws Exception {
        mockMvc.perform(get("/api/buyers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // 测试用例 S-007：处理购买意向
    @Test
    public void testHandlePurchaseIntention() throws Exception {
        // 先创建测试商品和购买意向
        Product product = new Product();
        product.setName("意向测试商品");
        product.setPrice(100.0);
        String productJson = objectMapper.writeValueAsString(product);
        String productResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long productId = objectMapper.readTree(productResponse).get("id").asLong();

        Buyer buyer = new Buyer();
        buyer.setName("测试买家");
        buyer.setPhone("13800138000");
        buyer.setAddress("测试地址");
        String buyerJson = objectMapper.writeValueAsString(buyer);
        String buyerResponse = mockMvc.perform(post("/api/buyers/product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyerJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long buyerId = objectMapper.readTree(buyerResponse).get("id").asLong();

        // 测试标记交易成功
        mockMvc.perform(put("/api/buyers/" + buyerId + "/complete")
                        .param("success", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}