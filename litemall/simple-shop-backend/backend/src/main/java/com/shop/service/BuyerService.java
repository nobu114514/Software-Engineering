package com.shop.service;

import com.shop.model.Buyer;
import com.shop.model.Customer;
import com.shop.model.Product;
import com.shop.repository.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// 自定义异常类，用于区分不同类型的错误
class BuyerServiceException extends RuntimeException {
    public BuyerServiceException(String message) {
        super(message);
    }
}

@Service
public class BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CustomerService customerService;

    @Transactional
    public Buyer createBuyer(Buyer buyer, Long productId, String username) {
        // 验证参数
        if (buyer == null) {
            throw new BuyerServiceException("购买意向信息不能为空");
        }
        
        // 首先检查产品是否存在
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new BuyerServiceException("商品不存在或已下架"));
        
        // 检查商品状态，确保可购买
        if (!product.isActive()) {
            throw new BuyerServiceException("该商品已下架，无法购买");
        }
        
        // 设置商品信息
        buyer.setProduct(product);
        
        // 1. 根据用户名获取客户信息，找到对应的customers表中的用户记录
        Optional<Customer> customerOpt = customerService.findByUsername(username);
        if (!customerOpt.isPresent()) {
            // 如果找不到对应的用户，抛出异常
            throw new BuyerServiceException("用户信息不存在，请重新登录");
        }
        
        // 2. 获取登录用户的客户ID（来自customers表的id字段）
        Long customerId = customerOpt.get().getId();
        
        // 3. 验证客户ID的有效性（额外的安全检查）
        if (!customerService.findById(customerId).isPresent()) {
            throw new BuyerServiceException("客户信息验证失败");
        }
        
        // 4. 将customers表中的用户ID设置为buyers表的customer_id字段
        buyer.setCustomerId(customerId);
        
        try {
            // 同时冻结商品
            productService.freezeProduct(productId, true);
            // 保存购买意向
            return buyerRepository.save(buyer);
        } catch (Exception e) {
            // 保存失败时，解冻商品
            try {
                productService.freezeProduct(productId, false);
            } catch (Exception ignore) {}
            throw new BuyerServiceException("创建购买意向失败：" + e.getMessage());
        }
    }
    

    public List<Buyer> getBuyersByProduct(Product product) {
        return buyerRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public boolean completeTransaction(Long buyerId, boolean success) {
        return buyerRepository.findById(buyerId).map(buyer -> {
            buyer.setCompleted(true);
            Product product = buyer.getProduct();
            
            if (success) {
                // 交易成功，商品下架
                productService.deactivateProduct(product.getId());
            } else {
                // 交易失败，商品解冻
                productService.freezeProduct(product.getId(), false);
            }
            
            buyerRepository.save(buyer);
            return true;
        }).orElse(false);
    }
}
