package com.shop.test.selenium.A;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductManagementTest extends UpgradeATestBase {
    private static final Logger logger = LoggerFactory.getLogger(ProductManagementTest.class);
    
    // 商品管理页面元素定位器
    private final By productManagementMenu = By.linkText("商品管理");
    private final By addProductButton = By.linkText("添加商品");
    private final By productNameField = By.id("name");
    private final By productPriceField = By.id("price");
    private final By productUnitField = By.id("unit");
    private final By productCategorySelect = By.id("category");
    private final By productDescriptionField = By.id("description");
    private final By submitProductButton = By.cssSelector("button[type='submit']");
    private final By productListTable = By.cssSelector("table.table");
    private final By editProductButton = By.linkText("编辑");
    private final By deleteProductButton = By.linkText("删除");
    private final By confirmDeleteButton = By.xpath("//button[contains(text(),'确定')]");
    private final By productSearchField = By.cssSelector("input[type='search']");
    private final By productSearchButton = By.cssSelector("button[type='button']");
    
    @BeforeEach
    public void beforeTest() {
        logger.info("=== 商品管理测试前置准备 ===");
        adminLogin("admin", "123456"); // 默认管理员账号密码
        
        // 导航到商品管理页面
        wait.until(ExpectedConditions.elementToBeClickable(productManagementMenu)).click();
        waitForPageLoad();
    }
    
    @AfterEach
    public void afterTest() {
        logger.info("=== 商品管理测试后置清理 ===");
        logout();
    }
    
    // 测试数据提供方法
    private Stream<Map<String, String>> productTestData() {
        String csvPath = "src/test/resources/product_management_test_data.csv";
        return loadTestData(csvPath);
    }
    
    // TC_PROD_001 - 商品列表显示测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testProductListDisplay(Map<String, String> testData) {
        if (!"TC_PROD_001".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_001
        }
        
        logger.info("执行测试用例：TC_PROD_001 - 商品列表显示测试");
        
        // 验证商品列表表格是否存在
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(productListTable));
        assertNotNull(table, "商品列表表格不存在");
        
        // 验证表格是否有数据行
        java.util.List<WebElement> rows = table.findElements(By.tagName("tr"));
        assertTrue(rows.size() > 1, "商品列表没有数据行");
        
        logger.info("TC_PROD_001 测试通过：商品列表显示正常");
    }
    
    // TC_PROD_002 - 添加新商品测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testAddProduct(Map<String, String> testData) {
        if (!"TC_PROD_002".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_002
        }
        
        logger.info("执行测试用例：TC_PROD_002 - 添加新商品测试");
        
        // 点击添加商品按钮
        wait.until(ExpectedConditions.elementToBeClickable(addProductButton)).click();
        waitForPageLoad();
        
        // 填写商品信息
        wait.until(ExpectedConditions.elementToBeClickable(productNameField)).sendKeys(testData.get("商品名称"));
        wait.until(ExpectedConditions.elementToBeClickable(productPriceField)).sendKeys(testData.get("价格"));
        wait.until(ExpectedConditions.elementToBeClickable(productUnitField)).sendKeys(testData.get("单位"));
        
        // 选择商品分类
        WebElement categorySelect = wait.until(ExpectedConditions.elementToBeClickable(productCategorySelect));
        categorySelect.sendKeys(testData.get("分类"));
        
        // 填写商品描述
        wait.until(ExpectedConditions.elementToBeClickable(productDescriptionField)).sendKeys(testData.get("描述"));
        
        // 提交表单
        wait.until(ExpectedConditions.elementToBeClickable(submitProductButton)).click();
        waitForPageLoad();
        
        // 验证添加成功
        WebElement successMsg = findElementSafely(successMessage, 5);
        if ("成功".equals(testData.get("预期结果"))) {
            assertNotNull(successMsg, "添加商品成功消息未显示");
            assertTrue(successMsg.isDisplayed(), "添加商品成功消息不可见");
        } else {
            WebElement errorMsg = findElementSafely(errorMessage, 5);
            assertNotNull(errorMsg, "添加商品失败消息未显示");
        }
        
        logger.info("TC_PROD_002 测试完成");
    }
    
    // TC_PROD_003 - 商品查询测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testProductSearch(Map<String, String> testData) {
        if (!"TC_PROD_003".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_003
        }
        
        logger.info("执行测试用例：TC_PROD_003 - 商品查询测试");
        
        // 输入查询关键词
        wait.until(ExpectedConditions.elementToBeClickable(productSearchField)).sendKeys(testData.get("搜索关键词"));
        wait.until(ExpectedConditions.elementToBeClickable(productSearchButton)).click();
        waitForPageLoad();
        
        // 验证搜索结果
        java.util.List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfElementLocated(productListTable))
                .findElements(By.tagName("tr"));
        
        if ("有结果".equals(testData.get("预期结果"))) {
            assertTrue(rows.size() > 1, "搜索应该有结果但没有找到");
        } else if ("无结果".equals(testData.get("预期结果"))) {
            // 检查是否有"无数据"提示或行数是否为1（只有表头）
            boolean hasNoData = rows.size() == 1 || 
                    driver.findElements(By.xpath("//*[contains(text(),'无数据')]")).size() > 0;
            assertTrue(hasNoData, "搜索应该无结果但找到了数据");
        }
        
        logger.info("TC_PROD_003 测试完成");
    }
    
    // TC_PROD_004 - 编辑商品信息测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testEditProduct(Map<String, String> testData) {
        if (!"TC_PROD_004".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_004
        }
        
        logger.info("执行测试用例：TC_PROD_004 - 编辑商品信息测试");
        
        // 先搜索要编辑的商品
        wait.until(ExpectedConditions.elementToBeClickable(productSearchField)).sendKeys(testData.get("商品名称"));
        wait.until(ExpectedConditions.elementToBeClickable(productSearchButton)).click();
        waitForPageLoad();
        
        // 点击编辑按钮
        java.util.List<WebElement> editButtons = driver.findElements(editProductButton);
        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();
            waitForPageLoad();
            
            // 修改商品信息
            WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(productNameField));
            nameField.clear();
            nameField.sendKeys(testData.get("新商品名称"));
            
            WebElement priceField = wait.until(ExpectedConditions.elementToBeClickable(productPriceField));
            priceField.clear();
            priceField.sendKeys(testData.get("新价格"));
            
            // 提交修改
            wait.until(ExpectedConditions.elementToBeClickable(submitProductButton)).click();
            waitForPageLoad();
            
            // 验证编辑成功
            WebElement successMsg = findElementSafely(successMessage, 5);
            assertNotNull(successMsg, "编辑商品成功消息未显示");
        } else {
            logger.warn("未找到要编辑的商品：{}", testData.get("商品名称"));
        }
        
        logger.info("TC_PROD_004 测试完成");
    }
    
    // TC_PROD_005 - 删除商品测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testDeleteProduct(Map<String, String> testData) {
        if (!"TC_PROD_005".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_005
        }
        
        logger.info("执行测试用例：TC_PROD_005 - 删除商品测试");
        
        // 先搜索要删除的商品
        wait.until(ExpectedConditions.elementToBeClickable(productSearchField)).sendKeys(testData.get("商品名称"));
        wait.until(ExpectedConditions.elementToBeClickable(productSearchButton)).click();
        waitForPageLoad();
        
        // 点击删除按钮
        java.util.List<WebElement> deleteButtons = driver.findElements(deleteProductButton);
        if (!deleteButtons.isEmpty()) {
            deleteButtons.get(0).click();
            
            // 处理确认对话框
            try {
                WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteButton));
                confirmButton.click();
                waitForPageLoad();
                
                // 验证删除成功
                WebElement successMsg = findElementSafely(successMessage, 5);
                assertNotNull(successMsg, "删除商品成功消息未显示");
            } catch (Exception e) {
                logger.error("处理删除确认对话框时出错：{}", e.getMessage());
            }
        } else {
            logger.warn("未找到要删除的商品：{}", testData.get("商品名称"));
        }
        
        logger.info("TC_PROD_005 测试完成");
    }
    
    // TC_PROD_006 - 商品库存管理测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testProductInventoryManagement(Map<String, String> testData) {
        if (!"TC_PROD_006".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_006
        }
        
        logger.info("执行测试用例：TC_PROD_006 - 商品库存管理测试");
        
        // 查找库存管理相关元素并执行操作
        WebElement inventoryManagementLink = findElementSafely(By.linkText("库存管理"), 5);
        if (inventoryManagementLink != null) {
            inventoryManagementLink.click();
            waitForPageLoad();
            
            // 实现库存管理逻辑...
            // 这里需要根据实际页面结构补充
            
            logger.info("TC_PROD_006 库存管理测试完成");
        } else {
            logger.warn("未找到库存管理功能入口");
        }
    }
    
    // TC_PROD_007 - 商品分类管理测试
    @ParameterizedTest
    @MethodSource("productTestData")
    public void testProductCategoryManagement(Map<String, String> testData) {
        if (!"TC_PROD_007".equals(testData.get("用例ID"))) {
            return; // 只执行TC_PROD_007
        }
        
        logger.info("执行测试用例：TC_PROD_007 - 商品分类管理测试");
        
        // 查找分类管理相关元素并执行操作
        WebElement categoryManagementLink = findElementSafely(By.linkText("分类管理"), 5);
        if (categoryManagementLink != null) {
            categoryManagementLink.click();
            waitForPageLoad();
            
            // 实现分类管理逻辑...
            // 这里需要根据实际页面结构补充
            
            logger.info("TC_PROD_007 分类管理测试完成");
        } else {
            logger.warn("未找到分类管理功能入口");
        }
    }
}