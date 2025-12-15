package com.shop.repository;

import com.shop.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // 根据用户名和商品ID查找收藏
    Optional<Favorite> findByUsernameAndProductId(String username, Long productId);
    
    // 根据用户名查找所有收藏
    List<Favorite> findByUsername(String username);
    
    // 删除收藏
    void deleteByUsernameAndProductId(String username, Long productId);
}