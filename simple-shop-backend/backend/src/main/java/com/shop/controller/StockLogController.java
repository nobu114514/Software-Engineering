package com.shop.controller;

import com.shop.dto.StockLogDTO;
import com.shop.model.StockLog;
import com.shop.service.StockLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/stock-logs")
public class StockLogController {
    private static final Logger logger = LoggerFactory.getLogger(StockLogController.class);
    
    @Autowired
    private StockLogService stockLogService;

    @GetMapping
    public ResponseEntity<List<StockLogDTO>> getAllStockLogs(@RequestParam(required = false) String productName) {
        try {
            List<StockLog> stockLogs;
            if (productName != null && !productName.isEmpty()) {
                // 如果提供了商品名称，搜索包含该名称的库存日志
                stockLogs = stockLogService.getStockLogsByProductNameContaining(productName);
            } else {
                // 否则获取所有库存日志
                stockLogs = stockLogService.getAllStockLogs();
            }
            
            // Convert to DTO to avoid cyclic references
            List<StockLogDTO> stockLogDTOs = new ArrayList<>();
            for (StockLog stockLog : stockLogs) {
                String productNameStr = (stockLog.getProduct() != null) ? stockLog.getProduct().getName() : "Unknown Product";
                StockLogDTO dto = new StockLogDTO(
                    stockLog.getId(),
                    productNameStr,
                    stockLog.getChangeQuantity(),
                    stockLog.getPreviousStock(),
                    stockLog.getCurrentStock(),
                    stockLog.getAction(),
                    stockLog.getDescription(),
                    stockLog.getCreatedAt()
                );
                stockLogDTOs.add(dto);
            }
            return ResponseEntity.ok(stockLogDTOs);
        } catch (Exception e) {
            logger.error("Failed to get stock logs", e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockLogDTO>> getStockLogsByProductId(@PathVariable Long productId) {
        try {
            List<StockLog> stockLogs = stockLogService.getStockLogsByProductId(productId);
            
            // Convert to DTO to avoid cyclic references
            List<StockLogDTO> stockLogDTOs = new ArrayList<>();
            for (StockLog stockLog : stockLogs) {
                String productNameStr = (stockLog.getProduct() != null) ? stockLog.getProduct().getName() : "Unknown Product";
                StockLogDTO dto = new StockLogDTO(
                    stockLog.getId(),
                    productNameStr,
                    stockLog.getChangeQuantity(),
                    stockLog.getPreviousStock(),
                    stockLog.getCurrentStock(),
                    stockLog.getAction(),
                    stockLog.getDescription(),
                    stockLog.getCreatedAt()
                );
                stockLogDTOs.add(dto);
            }
            return ResponseEntity.ok(stockLogDTOs);
        } catch (Exception e) {
            logger.error("Failed to get stock logs for product " + productId, e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
}