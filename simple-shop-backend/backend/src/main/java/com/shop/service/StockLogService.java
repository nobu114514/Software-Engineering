package com.shop.service;

import com.shop.model.Product;
import com.shop.model.StockLog;
import com.shop.repository.StockLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockLogService {
    @Autowired
    private StockLogRepository stockLogRepository;

    public void createStockLog(Product product, int changeQuantity, int previousStock, int currentStock, String action, String description) {
        StockLog stockLog = new StockLog();
        stockLog.setProduct(product);
        stockLog.setChangeQuantity(changeQuantity);
        stockLog.setPreviousStock(previousStock);
        stockLog.setCurrentStock(currentStock);
        stockLog.setAction(action);
        stockLog.setDescription(description);
        stockLog.setCreatedAt(LocalDateTime.now());
        stockLogRepository.save(stockLog);
    }

    @Transactional(readOnly = true)
    public List<StockLog> getStockLogsByProductId(Long productId) {
        return stockLogRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Transactional(readOnly = true)
    public List<StockLog> getAllStockLogs() {
        return stockLogRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<StockLog> getStockLogsByProductNameContaining(String productName) {
        return stockLogRepository.findByProductNameContainingOrderByCreatedAtDesc(productName);
    }
}