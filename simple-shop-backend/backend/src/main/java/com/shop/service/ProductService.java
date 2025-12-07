package com.shop.service;

import com.shop.model.Product;
import com.shop.model.SubCategory;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    // 获取所有上架商品
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public List<Product> getProductsBySubCategory(SubCategory subCategory) {
        return productRepository.findBySubCategoryAndIsActiveTrue(subCategory);
    }

    public List<Product> getProductsBySubCategoryId(Long subCategoryId) {
        return productRepository.findBySubCategoryId(subCategoryId);
    }
    
    // 获取指定子分类的上架商品
    public List<Product> getActiveProductsBySubCategoryId(Long subCategoryId) {
        return productRepository.findBySubCategoryIdAndIsActiveTrue(subCategoryId);
    }
    
    // 添加通过一级分类ID获取商品的方法
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findBySubCategoryCategoryId(categoryId);
    }
    
    // 获取指定一级分类的上架商品
    public List<Product> getActiveProductsByCategoryId(Long categoryId) {
        return productRepository.findBySubCategoryCategoryIdAndIsActiveTrue(categoryId);
    }

    public Product createProduct(Product product) {
        // 允许多个商品同时激活，移除限制逻辑
        product.setActive(true);
        product.setFrozen(false);
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

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

    public Product activateProduct(Long id) {
        // 只激活指定商品，不移除其他商品的激活状态
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(true);
            product.setFrozen(false); // 可选，确保未被冻结
            return productRepository.save(product);
        }
        return null;
    }
}