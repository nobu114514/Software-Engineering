package com.shop.service;

import com.shop.model.Seller;
import com.shop.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    // 初始化卖家账号，实际应用中应该在系统部署后手动创建
    @Transactional
    public void initializeSeller() {
        if (sellerRepository.count() == 0) {
            Seller seller = new Seller();
            seller.setUsername("seller");
            seller.setPassword("password"); // 实际应用中应加密存储
            sellerRepository.save(seller);
        }
    }

    public Optional<Seller> login(String username, String password) {
        return sellerRepository.findByUsername(username)
                .filter(seller -> seller.getPassword().equals(password));
    }

    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        return sellerRepository.findByUsername(username)
                .filter(seller -> seller.getPassword().equals(oldPassword))
                .map(seller -> {
                    seller.setPassword(newPassword);
                    sellerRepository.save(seller);
                    return true;
                })
                .orElse(false);
    }
}
