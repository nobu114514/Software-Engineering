package com.shop.test.api.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SellerLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 测试用例 S-001：卖家登录功能
    @Test
    public void testSellerLogin_Success() throws Exception {
        mockMvc.perform(post("/api/seller/login")
                        .param("username", "seller")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testSellerLogin_Failure() throws Exception {
        mockMvc.perform(post("/api/seller/login")
                        .param("username", "seller")
                        .param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
