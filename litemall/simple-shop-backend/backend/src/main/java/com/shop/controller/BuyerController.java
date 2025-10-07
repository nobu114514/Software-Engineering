package com.shop.controller;

import com.shop.model.Buyer;
import com.shop.service.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyers")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    @PostMapping("/product/{productId}")
    public ResponseEntity<Buyer> createBuyer(@RequestBody Buyer buyer, @PathVariable Long productId) {
        Buyer created = buyerService.createBuyer(buyer, productId);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public List<Buyer> getAllBuyers() {
        return buyerService.getAllBuyers();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Boolean> completeTransaction(
            @PathVariable Long id, 
            @RequestParam boolean success) {
        boolean result = buyerService.completeTransaction(id, success);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
}
