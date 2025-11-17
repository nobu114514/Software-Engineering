package com.shop.service;

import com.shop.model.Product;
import com.shop.model.SubCategory;
import com.shop.repository.ProductRepository;
import com.shop.repository.SubCategoryRepository;
import com.shop.repository.BuyerRepository;
import com.shop.repository.StockLogRepository;
import com.shop.service.StockLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockLogService stockLogService;
    
    @Autowired
    private SubCategoryRepository subCategoryRepository;
    
    @Autowired
    private BuyerRepository buyerRepository;
    
    @Autowired
    private StockLogRepository stockLogRepository;

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

    // 搜索商品（支持分页和排序）
    public Page<Product> searchProducts(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        
        return productRepository.searchActiveProducts(keyword.trim(), pageable);
    }

    // 按一级分类搜索商品
    public Page<Product> searchProductsByCategory(Long categoryId, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        
        return productRepository.searchActiveProductsByCategory(categoryId, keyword.trim(), pageable);
    }

    // 按二级分类搜索商品
    public Page<Product> searchProductsBySubCategory(Long subCategoryId, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        
        return productRepository.searchActiveProductsBySubCategory(subCategoryId, keyword.trim(), pageable);
    }

    public Product createProduct(Product product) {
        // 允许多个商品同时激活，移除限制逻辑
        product.setActive(true);
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product updateProduct(Product product) {
        // Find existing product to preserve active/frozen status
        return productRepository.findById(product.getId())
            .map(existingProduct -> {
                // Update only specific fields while preserving active/frozen status
                existingProduct.setName(product.getName());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setImageUrl(product.getImageUrl());
                existingProduct.setPrice(product.getPrice());
                
                // Record stock change if stock is different
                if (existingProduct.getStock() != product.getStock()) {
                    int oldStock = existingProduct.getStock();
                    int newStock = product.getStock();
                    String action = newStock > oldStock ? "INCREASE" : "DECREASE";
                    String description = "管理员修改商品库存";
                    existingProduct.setStock(newStock);
                    // Save the updated product first
                    Product saved = productRepository.save(existingProduct);
                    // Log the stock change
                    stockLogService.createStockLog(saved, newStock - oldStock, oldStock, newStock, action, description);
                } else {
                    existingProduct.setStock(product.getStock());
                }
                
                // Load sub-category from database to maintain proper relationships
                if (product.getSubCategory() != null && product.getSubCategory().getId() != null) {
                    subCategoryRepository.findById(product.getSubCategory().getId())
                        .ifPresent(existingProduct::setSubCategory);
                }
                
                // If stock is greater than 0, set product to active
                if (existingProduct.getStock() > 0) {
                    existingProduct.setActive(true);
                }
                
                // Preserve active/frozen status
                // existingProduct.setActive(product.isActive());
                // existingProduct.setFrozen(product.isFrozen());
                
                return productRepository.save(existingProduct);
            })
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + product.getId()));
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
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(true);
            return productRepository.save(product);
        }
        return null;
    }

    // 减少库存
    @Transactional
    public Product decreaseStock(Product product, int quantity) {
        if (product != null && product.getStock() >= quantity) {
            int oldStock = product.getStock();
            int newStock = oldStock - quantity;
            product.setStock(newStock);
            // Save the updated product
            Product saved = productRepository.save(product);
            // Log the stock change
            stockLogService.createStockLog(saved, -quantity, oldStock, newStock, "DECREASE", "用户下单减少库存");
            return saved;
        }
        return null;
    }

    @Transactional
    public Product decreaseStock(Long productId, int quantity) {
        Optional<Product> productOptional = productRepository.findById(productId);
        return productOptional.map(product -> decreaseStock(product, quantity)).orElse(null);
    }

    // 增加销量
    @Transactional
    public Product increaseSalesCount(Long productId, int quantity) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            int currentSales = product.getSalesCount();
            product.setSalesCount(currentSales + quantity);
            return productRepository.save(product);
        }
        return null;
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            // 先删除相关的库存日志记录
            stockLogRepository.deleteByProductId(id);
            // 再删除相关的购买意向记录
            buyerRepository.deleteByProductId(id);
            // 最后删除商品
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}