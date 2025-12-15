package com.shop.test.unit;

import com.shop.model.Product;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository; // 模拟Repository

    @InjectMocks
    private ProductService productService;

    // 测试用例 SER-001：冻结存在的商品
    @Test
    public void testFreezeProduct_Exists() {
        // 输入/前置条件：构造存在的商品
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setFrozen(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // 执行冻结操作
        boolean result = productService.freezeProduct(1L, true);

        // 预期结果
        assertTrue(result, "冻结存在的商品应返回true");
        assertTrue(mockProduct.isFrozen(), "商品状态应更新为冻结");
        verify(productRepository).save(mockProduct); // 验证执行了保存
    }

    // 测试用例 SER-002：冻结不存在的商品
    @Test
    public void testFreezeProduct_NotExists() {
        // 输入/前置条件：商品ID不存在
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行冻结操作
        boolean result = productService.freezeProduct(999L, true);

        // 预期结果
        assertFalse(result, "冻结不存在的商品应返回false");
        verify(productRepository, never()).save(any()); // 验证未执行保存
    }

    // 测试用例 SER-003：解冻商品（取消冻结）
    @Test
    public void testFreezeProduct_Unfreeze() {
        // 输入/前置条件：商品已冻结
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setFrozen(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // 执行解冻操作
        boolean result = productService.freezeProduct(1L, false);

        // 预期结果
        assertTrue(result, "解冻商品应返回true");
        assertFalse(mockProduct.isFrozen(), "商品状态应更新为未冻结");
        verify(productRepository).save(mockProduct);
    }
}