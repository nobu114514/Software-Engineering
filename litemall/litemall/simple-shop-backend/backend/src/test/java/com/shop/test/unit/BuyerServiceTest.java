package com.shop.test.unit;

import com.shop.model.Buyer;
import com.shop.model.Product;
import com.shop.repository.BuyerRepository;
import com.shop.service.BuyerService;
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
public class BuyerServiceTest {

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private BuyerService buyerService;

    // 测试用例 SER-004：标记交易成功
    @Test
    public void testCompleteTransaction_Success() {
        // 输入/前置条件：构造未处理的购买意向
        Product mockProduct = new Product();
        mockProduct.setId(1L);

        Buyer mockBuyer = new Buyer();
        mockBuyer.setId(1L);
        mockBuyer.setProduct(mockProduct);
        mockBuyer.setCompleted(false);
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(mockBuyer));

        // 执行交易成功操作
        boolean result = buyerService.completeTransaction(1L, true);

        // 预期结果
        assertTrue(result, "交易成功处理应返回true");
        assertTrue(mockBuyer.isCompleted(), "意向状态应更新为已完成");
        verify(productService).deactivateProduct(1L); // 验证商品已下架
        verify(buyerRepository).save(mockBuyer);
    }

    // 测试用例 SER-005：标记交易失败
    @Test
    public void testCompleteTransaction_Failure() {
        // 输入/前置条件：构造未处理的购买意向
        Product mockProduct = new Product();
        mockProduct.setId(1L);

        Buyer mockBuyer = new Buyer();
        mockBuyer.setId(1L);
        mockBuyer.setProduct(mockProduct);
        mockBuyer.setCompleted(false);
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(mockBuyer));

        // 执行交易失败操作
        boolean result = buyerService.completeTransaction(1L, false);

        // 预期结果
        assertTrue(result, "交易失败处理应返回true");
        assertTrue(mockBuyer.isCompleted(), "意向状态应更新为已完成");
        verify(productService).freezeProduct(1L, false); // 验证商品已解冻
        verify(buyerRepository).save(mockBuyer);
    }

    // 测试用例 SER-006：处理不存在的意向
    @Test
    public void testCompleteTransaction_NotExists() {
        // 输入/前置条件：意向ID不存在
        when(buyerRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行处理操作
        boolean result = buyerService.completeTransaction(999L, true);

        // 预期结果
        assertFalse(result, "处理不存在的意向应返回false");
        // 关键修复：使用具体类型匹配器替代any()
        verify(productService, never()).deactivateProduct(anyLong());
        verify(productService, never()).freezeProduct(anyLong(), anyBoolean());
    }
}
