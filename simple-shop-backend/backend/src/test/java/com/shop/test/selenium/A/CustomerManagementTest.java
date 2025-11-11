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
public class CustomerManagementTest extends UpgradeATestBase {
    private static final Logger logger = LoggerFactory.getLogger(CustomerManagementTest.class);
    
    // 客户管理页面元素定位器
    private final By customerManagementMenu = By.linkText("客户管理");
    private final By customerListTable = By.cssSelector("table.table");
    private final By customerSearchField = By.cssSelector("input[type='search']");
    private final By customerSearchButton = By.cssSelector("button[type='button']");
    private final By viewCustomerButton = By.linkText("查看");
    private final By editCustomerButton = By.linkText("编辑");
    private final By customerNameField = By.id("name");
    private final By customerPhoneField = By.id("phone");
    private final By customerEmailField = By.id("email");
    private final By customerAddressField = By.id("address");
    private final By submitCustomerButton = By.cssSelector("button[type='submit']");
    private final By customerLevelSelect = By.id("level");
    private final By customerStatisticsLink = By.linkText("客户统计");
    private final By customerGroupLink = By.linkText("客户分组");
    
    @BeforeEach
    public void beforeTest() {
        logger.info("=== 客户管理测试前置准备 ===");
        adminLogin("admin", "123456"); // 默认管理员账号密码
        
        // 导航到客户管理页面
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(customerManagementMenu)).click();
        waitForPageLoad();
    }
    
    @AfterEach
    public void afterTest() {
        logger.info("=== 客户管理测试后置清理 ===");
        logout();
    }
    
    // 测试数据提供方法
    private Stream<Map<String, String>> customerTestData() {
        String csvPath = "src/test/resources/customer_management_test_data.csv";
        return loadTestData(csvPath);
    }
    
    // TC_CUST_001 - 客户列表显示测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testCustomerListDisplay(Map<String, String> testData) {
        if (!"TC_CUST_001".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_001
        }
        
        logger.info("执行测试用例：TC_CUST_001 - 客户列表显示测试");
        
        // 验证客户列表表格是否存在
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(customerListTable));
        assertNotNull(table, "客户列表表格不存在");
        
        // 验证表格是否有数据行
        java.util.List<WebElement> rows = table.findElements(By.tagName("tr"));
        assertTrue(rows.size() > 1, "客户列表没有数据行");
        
        // 验证列表包含预期的字段
        WebElement header = rows.get(0);
        assertTrue(header.getText().contains("客户名称"), "表头不包含客户名称字段");
        assertTrue(header.getText().contains("联系方式"), "表头不包含联系方式字段");
        assertTrue(header.getText().contains("客户等级"), "表头不包含客户等级字段");
        
        logger.info("TC_CUST_001 测试通过：客户列表显示正常");
    }
    
    // TC_CUST_002 - 客户查询测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testCustomerSearch(Map<String, String> testData) {
        if (!"TC_CUST_002".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_002
        }
        
        logger.info("执行测试用例：TC_CUST_002 - 客户查询测试");
        
        // 输入查询关键词
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchField)).sendKeys(testData.get("搜索关键词"));
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchButton)).click();
        waitForPageLoad();
        
        // 验证搜索结果
        java.util.List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfElementLocated(customerListTable))
                .findElements(By.tagName("tr"));
        
        if ("有结果".equals(testData.get("预期结果"))) {
            assertTrue(rows.size() > 1, "搜索应该有结果但没有找到");
        } else if ("无结果".equals(testData.get("预期结果"))) {
            // 检查是否有"无数据"提示或行数是否为1（只有表头）
            boolean hasNoData = rows.size() == 1 || 
                    driver.findElements(By.xpath("//*[contains(text(),'无数据')]")).size() > 0;
            assertTrue(hasNoData, "搜索应该无结果但找到了数据");
        }
        
        logger.info("TC_CUST_002 测试完成");
    }
    
    // TC_CUST_003 - 客户详情查看测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testCustomerDetailView(Map<String, String> testData) {
        if (!"TC_CUST_003".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_003
        }
        
        logger.info("执行测试用例：TC_CUST_003 - 客户详情查看测试");
        
        // 先搜索要查看的客户
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchField)).sendKeys(testData.get("客户名称"));
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchButton)).click();
        waitForPageLoad();
        
        // 点击查看按钮
        java.util.List<WebElement> viewButtons = driver.findElements(viewCustomerButton);
        if (!viewButtons.isEmpty()) {
            viewButtons.get(0).click();
            waitForPageLoad();
            
            // 验证详情页面包含客户信息
            String pageSource = driver.getPageSource();
            assertTrue(pageSource.contains(testData.get("客户名称")), "详情页面不包含客户名称");
            
            // 返回列表页
            driver.navigate().back();
            waitForPageLoad();
        } else {
            logger.warn("未找到要查看的客户：{}", testData.get("客户名称"));
        }
        
        logger.info("TC_CUST_003 测试完成");
    }
    
    // TC_CUST_004 - 客户信息编辑测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testEditCustomer(Map<String, String> testData) {
        if (!"TC_CUST_004".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_004
        }
        
        logger.info("执行测试用例：TC_CUST_004 - 客户信息编辑测试");
        
        // 先搜索要编辑的客户
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchField)).sendKeys(testData.get("客户名称"));
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchButton)).click();
        waitForPageLoad();
        
        // 点击编辑按钮
        java.util.List<WebElement> editButtons = driver.findElements(editCustomerButton);
        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();
            waitForPageLoad();
            
            // 修改客户信息
            if (testData.get("新联系电话") != null && !testData.get("新联系电话").isEmpty()) {
                WebElement phoneField = wait.until(ExpectedConditions.elementToBeClickable(customerPhoneField));
                phoneField.clear();
                phoneField.sendKeys(testData.get("新联系电话"));
            }
            
            if (testData.get("新邮箱") != null && !testData.get("新邮箱").isEmpty()) {
                WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(customerEmailField));
                emailField.clear();
                emailField.sendKeys(testData.get("新邮箱"));
            }
            
            if (testData.get("新地址") != null && !testData.get("新地址").isEmpty()) {
                WebElement addressField = wait.until(ExpectedConditions.elementToBeClickable(customerAddressField));
                addressField.clear();
                addressField.sendKeys(testData.get("新地址"));
            }
            
            // 提交修改
            wait.until(ExpectedConditions.elementToBeClickable(submitCustomerButton)).click();
            waitForPageLoad();
            
            // 验证编辑成功
            WebElement successMsg = findElementSafely(successMessage, 5);
            assertNotNull(successMsg, "编辑客户成功消息未显示");
        } else {
            logger.warn("未找到要编辑的客户：{}", testData.get("客户名称"));
        }
        
        logger.info("TC_CUST_004 测试完成");
    }
    
    // TC_CUST_005 - 客户等级管理测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testCustomerLevelManagement(Map<String, String> testData) {
        if (!"TC_CUST_005".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_005
        }
        
        logger.info("执行测试用例：TC_CUST_005 - 客户等级管理测试");
        
        // 先搜索要修改等级的客户
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchField)).sendKeys(testData.get("客户名称"));
        wait.until(ExpectedConditions.elementToBeClickable(customerSearchButton)).click();
        waitForPageLoad();
        
        // 点击编辑按钮
        java.util.List<WebElement> editButtons = driver.findElements(editCustomerButton);
        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();
            waitForPageLoad();
            
            // 修改客户等级
            WebElement levelSelect = wait.until(ExpectedConditions.elementToBeClickable(customerLevelSelect));
            levelSelect.sendKeys(testData.get("客户等级"));
            
            // 提交修改
            wait.until(ExpectedConditions.elementToBeClickable(submitCustomerButton)).click();
            waitForPageLoad();
            
            // 验证修改成功
            WebElement successMsg = findElementSafely(successMessage, 5);
            assertNotNull(successMsg, "修改客户等级成功消息未显示");
        } else {
            logger.warn("未找到要修改等级的客户：{}", testData.get("客户名称"));
        }
        
        logger.info("TC_CUST_005 测试完成");
    }
    
    // TC_CUST_006 - 客户统计功能测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testCustomerStatistics(Map<String, String> testData) {
        if (!"TC_CUST_006".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_006
        }
        
        logger.info("执行测试用例：TC_CUST_006 - 客户统计功能测试");
        
        // 查找客户统计链接并点击
        WebElement statisticsLink = findElementSafely(customerStatisticsLink, 5);
        if (statisticsLink != null) {
            statisticsLink.click();
            waitForPageLoad();
            
            // 验证统计页面包含预期内容
            assertTrue(driver.getPageSource().contains("客户总数"), "统计页面不包含客户总数");
            assertTrue(driver.getPageSource().contains("销售额统计"), "统计页面不包含销售额统计");
            
            // 验证图表或数据是否显示
            java.util.List<WebElement> charts = driver.findElements(By.cssSelector("canvas, .chart"));
            assertTrue(charts.size() > 0, "统计页面没有图表显示");
            
            logger.info("TC_CUST_006 测试通过：客户统计功能正常");
        } else {
            logger.warn("未找到客户统计功能入口");
        }
    }
    
    // TC_CUST_007 - 客户分组管理测试
    @ParameterizedTest
    @MethodSource("customerTestData")
    public void testCustomerGroupManagement(Map<String, String> testData) {
        if (!"TC_CUST_007".equals(testData.get("用例ID"))) {
            return; // 只执行TC_CUST_007
        }
        
        logger.info("执行测试用例：TC_CUST_007 - 客户分组管理测试");
        
        // 查找客户分组链接并点击
        WebElement groupLink = findElementSafely(customerGroupLink, 5);
        if (groupLink != null) {
            groupLink.click();
            waitForPageLoad();
            
            // 验证分组管理页面功能
            // 这里需要根据实际页面结构补充具体的测试逻辑
            
            logger.info("TC_CUST_007 测试完成：客户分组管理功能");
        } else {
            logger.warn("未找到客户分组管理功能入口");
        }
    }
}