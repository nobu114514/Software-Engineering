package com.shop.service;

import com.shop.model.Category;
import com.shop.model.SubCategory;
import com.shop.repository.CategoryRepository;
import com.shop.repository.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataInitializerService implements ApplicationRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化一级分类数据
        if (categoryRepository.count() == 0) {
            initCategories();
        }

        // 初始化二级分类数据
        if (subCategoryRepository.count() == 0) {
            initSubCategories();
        }
    }

    private void initCategories() {
        // 创建一级分类
        Category digital = new Category();
        digital.setName("数码产品");
        digital.setDescription("各类数码电子设备");
        digital.setActive(true);
        digital.setSortOrder(1);
        digital.setIcon("/icons/digital.png");
        categoryRepository.save(digital);

        Category clothing = new Category();
        clothing.setName("服装鞋帽");
        clothing.setDescription("时尚服装和鞋类");
        clothing.setActive(true);
        clothing.setSortOrder(2);
        clothing.setIcon("/icons/clothing.png");
        categoryRepository.save(clothing);

        Category home = new Category();
        home.setName("家居用品");
        home.setDescription("居家生活必备品");
        home.setActive(true);
        home.setSortOrder(3);
        home.setIcon("/icons/home.png");
        categoryRepository.save(home);

        Category food = new Category();
        food.setName("食品饮料");
        food.setDescription("各类食品和饮品");
        food.setActive(true);
        food.setSortOrder(4);
        food.setIcon("/icons/food.png");
        categoryRepository.save(food);

        Category beauty = new Category();
        beauty.setName("美妆个护");
        beauty.setDescription("美容和个人护理产品");
        beauty.setActive(true);
        beauty.setSortOrder(5);
        beauty.setIcon("/icons/beauty.png");
        categoryRepository.save(beauty);
    }

    private void initSubCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        // 为数码产品添加二级分类
        Category digitalCategory = categories.stream()
                .filter(c -> c.getName().equals("数码产品")).findFirst().orElse(null);
        if (digitalCategory != null) {
            SubCategory phone = new SubCategory();
            phone.setName("手机");
            phone.setDescription("智能手机和平板");
            phone.setActive(true);
            phone.setSortOrder(1);
            phone.setIcon("/icons/phone.png");
            phone.setCategory(digitalCategory);
            subCategoryRepository.save(phone);

            SubCategory computer = new SubCategory();
            computer.setName("电脑");
            computer.setDescription("笔记本和台式电脑");
            computer.setActive(true);
            computer.setSortOrder(2);
            computer.setIcon("/icons/computer.png");
            computer.setCategory(digitalCategory);
            subCategoryRepository.save(computer);

            SubCategory camera = new SubCategory();
            camera.setName("相机");
            camera.setDescription("数码相机和配件");
            camera.setActive(true);
            camera.setSortOrder(3);
            camera.setIcon("/icons/camera.png");
            camera.setCategory(digitalCategory);
            subCategoryRepository.save(camera);
        }

        // 为服装鞋帽添加二级分类
        Category clothingCategory = categories.stream()
                .filter(c -> c.getName().equals("服装鞋帽")).findFirst().orElse(null);
        if (clothingCategory != null) {
            SubCategory mens = new SubCategory();
            mens.setName("男装");
            mens.setDescription("男士服装");
            mens.setActive(true);
            mens.setSortOrder(1);
            mens.setIcon("/icons/mens.png");
            mens.setCategory(clothingCategory);
            subCategoryRepository.save(mens);

            SubCategory womens = new SubCategory();
            womens.setName("女装");
            womens.setDescription("女士服装");
            womens.setActive(true);
            womens.setSortOrder(2);
            womens.setIcon("/icons/womens.png");
            womens.setCategory(clothingCategory);
            subCategoryRepository.save(womens);

            SubCategory shoes = new SubCategory();
            shoes.setName("鞋类");
            shoes.setDescription("各类鞋子");
            shoes.setActive(true);
            shoes.setSortOrder(3);
            shoes.setIcon("/icons/shoes.png");
            shoes.setCategory(clothingCategory);
            subCategoryRepository.save(shoes);
        }

        // 为家居用品添加二级分类
        Category homeCategory = categories.stream()
                .filter(c -> c.getName().equals("家居用品")).findFirst().orElse(null);
        if (homeCategory != null) {
            SubCategory kitchen = new SubCategory();
            kitchen.setName("厨具");
            kitchen.setDescription("厨房用具");
            kitchen.setActive(true);
            kitchen.setSortOrder(1);
            kitchen.setIcon("/icons/kitchen.png");
            kitchen.setCategory(homeCategory);
            subCategoryRepository.save(kitchen);

            SubCategory bedroom = new SubCategory();
            bedroom.setName("卧室");
            bedroom.setDescription("卧室用品");
            bedroom.setActive(true);
            bedroom.setSortOrder(2);
            bedroom.setIcon("/icons/bedroom.png");
            bedroom.setCategory(homeCategory);
            subCategoryRepository.save(bedroom);

            SubCategory livingroom = new SubCategory();
            livingroom.setName("客厅");
            livingroom.setDescription("客厅用品");
            livingroom.setActive(true);
            livingroom.setSortOrder(3);
            livingroom.setIcon("/icons/livingroom.png");
            livingroom.setCategory(homeCategory);
            subCategoryRepository.save(livingroom);
        }
    }
}