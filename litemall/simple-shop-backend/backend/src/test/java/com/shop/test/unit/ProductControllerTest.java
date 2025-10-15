package com.shop.test.unit;

import com.shop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.shop.controller.ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    // 测试用例 CTR-001：调用冻结接口（参数正确）
    @Test
    public void testFreezeProductApi_ValidParam() throws Exception {
        // 输入/前置条件：模拟服务层返回成功
        when(productService.freezeProduct(1L, true)).thenReturn(true);

        // 执行接口请求
        mockMvc.perform(put("/api/products/1/freeze")
                        .param("freeze", "true"))
                .andExpect(status().isOk()) // 预期响应状态
                .andExpect(content().string("true")); // 预期响应内容
    }

    // 测试用例 CTR-002：调用冻结接口（参数错误）
    @Test
    public void testFreezeProductApi_InvalidParam() throws Exception {
        // 输入/前置条件：参数为非布尔值
        // 执行接口请求（参数错误）
        mockMvc.perform(put("/api/products/1/freeze")
                        .param("freeze", "abc")) // 错误参数
                .andExpect(status().isBadRequest()); // 预期400响应
    }
}