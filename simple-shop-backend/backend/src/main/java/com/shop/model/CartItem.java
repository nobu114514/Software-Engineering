package com.shop.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart_items")
public class CartItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    private int quantity;
    
    // 用于前端显示的属性，不持久化到数据库
    @Transient
    private Long productId;
    
    @Transient
    private String productName;
    
    @Transient
    private String imageUrl;
    
    @Transient
    private double price;

    // 构造函数
    public CartItem() {
    }
    
    public CartItem(Customer customer, Product product, int quantity) {
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        // 同步Product的属性到临时属性
        if (product != null) {
            this.productId = product.getId();
            this.productName = product.getName();
            this.imageUrl = product.getImageUrl();
            this.price = product.getPrice();
        }
    }

    public Long getProductId() {
        return productId != null ? productId : (product != null ? product.getId() : null);
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName != null ? productName : (product != null ? product.getName() : null);
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : (product != null ? product.getImageUrl() : null);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price != 0 ? price : (product != null ? product.getPrice() : 0);
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // 增加商品数量
    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    // 减少商品数量
    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
        }
    }
    
    // 当从数据库加载时，确保临时字段被正确设置
    @PostLoad
    private void postLoad() {
        if (this.product != null) {
            this.productId = product.getId();
            this.productName = product.getName();
            this.imageUrl = product.getImageUrl();
            this.price = product.getPrice();
        }
    }
}