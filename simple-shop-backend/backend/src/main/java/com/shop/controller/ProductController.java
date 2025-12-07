package com.shop.controller;

import com.shop.model.Product;
import com.shop.service.ProductService;
import com.shop.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SubCategoryService subCategoryService;

    @GetMapping("/active")
    public ResponseEntity<Product> getActiveProduct() {
        Optional<Product> product = productService.getActiveProduct();
        return product.map(p -> {
            // 清除循环引用
            if (p.getSubCategory() != null) {
                p.getSubCategory().setCategory(null);
            }
            return ResponseEntity.ok(p);
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Product> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        // 清除循环引用
        for (Product product : products) {
            if (product.getSubCategory() != null) {
                product.getSubCategory().setCategory(null);
            }
        }
        return products;
    }
    
    // 获取所有上架商品
    @GetMapping("/active-list")
    public List<Product> getActiveProducts() {
        List<Product> products = productService.getActiveProducts();
        // 清除循环引用
        for (Product product : products) {
            if (product.getSubCategory() != null) {
                product.getSubCategory().setCategory(null);
            }
        }
        return products;
    }

    @GetMapping("/sub-category/{subCategoryId}")
    public ResponseEntity<List<Product>> getProductsBySubCategory(@PathVariable Long subCategoryId) {
        // 验证二级分类是否存在
        if (!subCategoryService.getSubCategoryById(subCategoryId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        List<Product> products = productService.getProductsBySubCategoryId(subCategoryId);
        // 清除循环引用
        for (Product product : products) {
            if (product.getSubCategory() != null) {
                product.getSubCategory().setCategory(null);
            }
        }
        return ResponseEntity.ok(products);
    }
    
    // 获取指定子分类的上架商品
    @GetMapping("/sub-category/{subCategoryId}/active")
    public ResponseEntity<List<Product>> getActiveProductsBySubCategory(@PathVariable Long subCategoryId) {
        // 验证二级分类是否存在
        if (!subCategoryService.getSubCategoryById(subCategoryId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        List<Product> products = productService.getActiveProductsBySubCategoryId(subCategoryId);
        // 清除循环引用
        for (Product product : products) {
            if (product.getSubCategory() != null) {
                product.getSubCategory().setCategory(null);
            }
        }
        return ResponseEntity.ok(products);
    }
    
    // 添加通过一级分类ID获取商品的API端点
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryId(categoryId);
        // 清除循环引用
        for (Product product : products) {
            if (product.getSubCategory() != null) {
                product.getSubCategory().setCategory(null);
            }
        }
        return ResponseEntity.ok(products);
    }
    
    // 获取指定一级分类的上架商品
    @GetMapping("/category/{categoryId}/active")
    public ResponseEntity<List<Product>> getActiveProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getActiveProductsByCategoryId(categoryId);
        // 清除循环引用
        for (Product product : products) {
            if (product.getSubCategory() != null) {
                product.getSubCategory().setCategory(null);
            }
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(p -> {
            // 清除循环引用
            if (p.getSubCategory() != null) {
                p.getSubCategory().setCategory(null);
            }
            return ResponseEntity.ok(p);
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        // 清除循环引用
        if (created.getSubCategory() != null) {
            created.getSubCategory().setCategory(null);
        }
        return created;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.getProductById(id)
                .map(existingProduct -> {
                    product.setId(id);
                    Product updated = productService.updateProduct(product);
                    // 清除循环引用
                    if (updated.getSubCategory() != null) {
                        updated.getSubCategory().setCategory(null);
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/freeze")
    public ResponseEntity<Boolean> freezeProduct(@PathVariable Long id, @RequestParam boolean freeze) {
        boolean success = productService.freezeProduct(id, freeze);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Boolean> deactivateProduct(@PathVariable Long id) {
        boolean success = productService.deactivateProduct(id);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // 激活（上架）某个商品，允许多个商品同时上架
    @PutMapping("/{id}/activate")
    public ResponseEntity<Product> activateProduct(@PathVariable Long id) {
        try {
            Product updated = productService.activateProduct(id);
            if (updated != null) {
                // 清除循环引用
                if (updated.getSubCategory() != null) {
                    updated.getSubCategory().setCategory(null);
                }
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}