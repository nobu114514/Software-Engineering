package com.shop.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "buyers")
public class Buyer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String phone;
    
    private String address;
    
    private String notes; // 购买备注
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    private boolean isCompleted; // 交易是否完成
    
    @Column(name = "order_status", nullable = false, columnDefinition = "int default 0")
    private int orderStatus; // 订单状态 0:客户下单, 1:商家确认, 2:备货完成, 3:开始发货, 4:交易完成, 5:交易失败
    
    private LocalDateTime createdAt;
    
    @Column(name = "customer_id")
    private Long customerId; // 客户ID
    
    // 默认构造函数
    public Buyer() {
        this.orderStatus = 0; // 设置默认订单状态为0
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isCompleted = false;
        orderStatus = 0; // 默认初始状态为客户下单
    }
}
