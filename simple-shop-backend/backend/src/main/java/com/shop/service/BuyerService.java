package com.shop.service;

import com.shop.exception.BuyerServiceException;
import com.shop.model.Buyer;
import com.shop.model.Customer;
import com.shop.model.Product;
import com.shop.repository.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CustomerService customerService;

    @Transactional
    public Buyer createBuyer(Buyer buyer, Long productId, String username) {
        // 验证参数
        if (buyer == null) {
            throw new BuyerServiceException("购买意向信息不能为空");
        }
        
        // 首先检查产品是否存在
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new BuyerServiceException("商品不存在或已下架"));
        
        // 检查商品状态，确保可购买
        if (!product.isActive()) {
            throw new BuyerServiceException("该商品已下架，无法购买");
        }
        
        // 检查库存，只有库存大于0时才允许购买
        if (product.getStock() <= 0) {
            throw new BuyerServiceException("该商品已售罄，无法购买");
        }
        
        // 设置商品信息
        buyer.setProduct(product);
        
        // 清除商品的subCategory，避免后续JSON序列化问题
        product.setSubCategory(null);
        
        // 1. 根据用户名获取客户信息，找到对应的customers表中的用户记录
        Optional<Customer> customerOpt = customerService.findByUsername(username);
        if (!customerOpt.isPresent()) {
            // 如果找不到对应的用户，抛出异常
            throw new BuyerServiceException("用户信息不存在，请重新登录");
        }
        
        // 2. 获取登录用户的客户ID（来自customers表的id字段）
        Long customerId = customerOpt.get().getId();
        
        // 3. 验证客户ID的有效性（额外的安全检查）
        if (!customerService.findById(customerId).isPresent()) {
            throw new BuyerServiceException("客户信息验证失败");
        }
        
        // 4. 将customers表中的用户ID设置为buyers表的customer_id字段
        buyer.setCustomerId(customerId);
        
        try {
            // 直接保存购买意向，不再冻结商品
            return buyerRepository.save(buyer);
        } catch (Exception e) {
            throw new BuyerServiceException("创建购买意向失败：" + e.getMessage());
        }
    }
    

    public List<Buyer> getBuyersByProduct(Product product) {
        return buyerRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public boolean completeTransaction(Long buyerId, boolean success) {
        return buyerRepository.findById(buyerId).map(buyer -> {
            buyer.setCompleted(true);
            if (success) {
                buyer.setOrderStatus(4); // 交易完成
                Product product = buyer.getProduct();
                // 交易成功，减少库存但不自动下架商品
                productService.decreaseStock(product.getId(), 1);
                // 同时增加销量
                productService.increaseSalesCount(product.getId(), 1);
            } else {
                buyer.setOrderStatus(5); // 交易失败
            }
            // 交易失败时不再需要解冻商品，因为商品不再被冻结
            
            buyerRepository.save(buyer);
            return true;
        }).orElse(false);
    }
    
    @Transactional
    public Buyer updateOrderStatus(Long buyerId, Integer status) {
        return buyerRepository.findById(buyerId).map(buyer -> {
            Integer currentStatus = buyer.getOrderStatus();
            
            // 如果当前状态为null，视为初始状态(0)
            if (currentStatus == null) {
                currentStatus = 0;
            }
            
            // 检查订单是否已经处于终态（交易完成或交易失败）
            if (currentStatus == 4 || currentStatus == 5) {
                throw new BuyerServiceException("该订单已处于终态，无法再进行操作");
            }
            
            // 验证状态值的有效性
            if (status == null || status < 0 || status > 5) {
                throw new BuyerServiceException("无效的订单状态值");
            }
            
            // 验证状态更新的顺序（只能向前更新，不能回退）
            if (status < currentStatus) {
                throw new BuyerServiceException("订单状态不能回退");
            }
            
            // 验证状态必须按顺序递增（只能递增1，不能跳过中间状态）
            // 但允许直接跳转到交易失败状态
            if (status > currentStatus + 1 && status != 5) {
                throw new BuyerServiceException("订单状态必须按顺序更新，不能跳过中间状态");
            }
            
            // 如果状态没有变化，直接返回buyer
            if (status.equals(currentStatus)) {
                return buyer;
            }
            
            Product product = buyer.getProduct();
            
            // 根据不同的状态转换执行相应的业务逻辑
            switch (status) {
                case 1: // 商家确认
                    // 检查商品库存是否充足
                    if (product.getStock() <= 0) {
                        throw new BuyerServiceException("商品库存不足，无法确认订单");
                    }
                    break;
                
                case 2: // 备货完成
                    // 备货完成时的业务逻辑
                    break;
                
                case 3: // 开始发货
                    // 开始发货时的业务逻辑
                    break;
                    
                case 4: // 交易完成
                    // 减少库存
                    productService.decreaseStock(product.getId(), 1);
                    // 增加销量
                    productService.increaseSalesCount(product.getId(), 1);
                    buyer.setCompleted(true);
                    break;
                    
                case 5: // 交易失败
                    // 交易失败时的业务逻辑
                    // 标记为已完成（终态）
                    buyer.setCompleted(true);
                    break;
            }
            
            // 更新订单状态
            buyer.setOrderStatus(status);
            return buyerRepository.save(buyer);
        }).orElseThrow(() -> new BuyerServiceException("购买意向不存在"));
    }
    
    @Transactional
    public Buyer cancelOrder(Long buyerId, String username, boolean isCustomer) {
        return buyerRepository.findById(buyerId).map(buyer -> {
            Integer currentStatus = buyer.getOrderStatus();
            
            // 如果当前状态为null，视为初始状态(0)
            if (currentStatus == null) {
                currentStatus = 0;
            }
            
            // 检查订单是否已经处于终态（交易完成或交易失败）
            if (currentStatus == 4 || currentStatus == 5) {
                throw new BuyerServiceException("该订单已处于终态，无法再进行操作");
            }
            
            if (isCustomer) {
                // 客户取消订单
                // 1. 验证是否是自己的订单
                Optional<Customer> customerOpt = customerService.findByUsername(username);
                if (!customerOpt.isPresent()) {
                    throw new BuyerServiceException("用户信息不存在，请重新登录");
                }
                
                Long customerId = customerOpt.get().getId();
                if (!customerId.equals(buyer.getCustomerId())) {
                    throw new BuyerServiceException("您无权取消此订单");
                }
                
                // 2. 检查订单状态是否允许取消（开始发货前，即状态<3）
                if (currentStatus >= 3) {
                    throw new BuyerServiceException("订单已开始发货，无法取消");
                }
            } else {
                // 商家取消订单
                // 1. 验证是否是自己商品的订单
                // 这里假设商家通过商品关联验证权限，实际项目中可能需要更复杂的权限验证
                // 检查订单状态是否允许取消（交易完成前，即状态<4）
                if (currentStatus >= 4) {
                    throw new BuyerServiceException("订单已交易完成，无法取消");
                }
            }
            
            // 执行取消操作，将订单状态设置为交易失败
            buyer.setOrderStatus(5);
            buyer.setCompleted(true);
            
            return buyerRepository.save(buyer);
        }).orElseThrow(() -> new BuyerServiceException("购买意向不存在"));
    }
}
