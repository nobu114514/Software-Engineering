package com.shop.service;

import com.shop.model.Buyer;
import com.shop.model.Product;
import com.shop.repository.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public Buyer createBuyer(Buyer buyer, Long productId) {
        productService.getProductById(productId).ifPresent(product -> {
            buyer.setProduct(product);
            // 同时冻结商品
            productService.freezeProduct(productId, true);
            buyerRepository.save(buyer);
        });
        return buyer;
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
