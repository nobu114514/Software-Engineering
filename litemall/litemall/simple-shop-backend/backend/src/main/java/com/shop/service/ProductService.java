package com.shop.service;

import com.shop.model.Product;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Optional<Product> getActiveProduct() {
        return productRepository.findByIsActiveTrue();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Product createProduct(Product product) {
        // 将任何现有的在售商品设为非活跃
        productRepository.findByIsActiveTrue().ifPresent(p -> {
            p.setActive(false);
            productRepository.save(p);
        });
        product.setActive(true);
        product.setFrozen(false);
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public boolean freezeProduct(Long id, boolean freeze) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setFrozen(freeze);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deactivateProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(false);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    // ✅ 新增方法：激活指定商品，并自动将其他商品设为非活跃
    @Transactional
    public Product activateProductAndDeactivateOthers(Long id) {
        // 1. 将所有商品设为非活跃
        List<Product> all = productRepository.findAll();
        for (Product p : all) {
            p.setActive(false);
            productRepository.save(p);
        }

        // 2. 激活传入的商品
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(true);
            product.setFrozen(false); // 可选，确保未被冻结
            return productRepository.save(product);
        } else {
            throw new RuntimeException("商品不存在，ID: " + id);
        }
    }
}