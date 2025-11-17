package com.shop.dto;

import java.time.LocalDateTime;

public class StockLogDTO {
    private Long id;
    private String productName;
    private int changeQuantity;
    private int previousStock;
    private int currentStock;
    private String action;
    private String description;
    private LocalDateTime createdAt;

    // Constructors
    public StockLogDTO() {}

    public StockLogDTO(Long id, String productName, int changeQuantity, int previousStock, int currentStock, String action, String description, LocalDateTime createdAt) {
        this.id = id;
        this.productName = productName;
        this.changeQuantity = changeQuantity;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.action = action;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getChangeQuantity() { return changeQuantity; }
    public void setChangeQuantity(int changeQuantity) { this.changeQuantity = changeQuantity; }

    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}