package com.shop.test.selenium.A;

import com.shop.test.selenium.DataDrivenTestBase;
import com.shop.test.selenium.LoginUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A升级包功能自动化测试
 * 测试A升级包的核心功能，包括角色管理、权限管理等
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AUpgradePackageTest extends DataDrivenTestBase {
    // 管理员登录信息
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    
    // 页面元素定位
    private final By roleManagementLink = By.xpath("//a[contains(text(), '角色管理')]");
    private final By permissionManagementLink = By.xpath("//a[contains(text(), '权限管理')]");
    private final By addRoleButton = By.xpath("//button[contains(text(), '新增角色')]");
    private final By roleNameField = By.xpath("//input[@name='roleName']");
    private final By roleDescriptionField = By.xpath("//textarea[@name='description']");
    private final By saveButton = By.xpath("//button[contains(text(), '保存')]");
    private final By successToast = By.xpath("//div[contains(@class, 'toast-success')]");
    private final By roleList = By.xpath("//table[@class='role-table']/tbody/tr");
    
    @BeforeEach
    public void beforeEachTest() {
        logger.info("=== 准备测试环境 ===");
        // 登录管理员账号
        boolean loggedIn = LoginUtils.login(driver, ADMIN_USERNAME, ADMIN_PASSWORD);
        if (!loggedIn) {
            logger.error("管理员登录失败，测试终止");
            Assertions.fail("管理员登录失败");
        }
        
        // 导航到A升级包功能模块页面
        navigateTo("/admin/a-upgrade");
    }
    
    @AfterEach
    public void afterEachTest() {
        logger.info("=== 清理测试环境 ===");
        // 退出登录
        LoginUtils.logout(driver);
    }
    
    /**
     * 加载测试数据
     */
    public Stream<Map<String, String>> testData() {
        // 可以从CSV文件加载数据，这里使用硬编码数据作为示例
        String csvPath = "src/test/resources/a_upgrade_test_data.csv";
        return loadTestData(csvPath);
    }
    
    /**
     * 测试角色管理功能
     */
    @ParameterizedTest
    @MethodSource("testData")
    public void testRoleManagement(Map<String, String> testCase) {
        logger.info("=== 执行测试用例: {} ===", testCase.get("用例 ID"));
        
        try {
            // 点击角色管理链接
            WebElement roleLink = wait.until(ExpectedConditions.elementToBeClickable(roleManagementLink));
            roleLink.click();
            waitForPageLoad();
            
            // 点击新增角色按钮
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addRoleButton));
            addBtn.click();
            
            // 输入角色信息
            String roleName = testCase.get("角色名称");
            String description = testCase.get("角色描述");
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(roleNameField)).sendKeys(roleName);
            wait.until(ExpectedConditions.visibilityOfElementLocated(roleDescriptionField)).sendKeys(description);
            
            // 保存角色
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
            saveBtn.click();
            
            // 验证保存成功
            if ("成功".equals(testCase.get("预期结果"))) {
                WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
                Assertions.assertTrue(success.isDisplayed(), "角色创建成功提示未显示");
                
                // 验证角色列表中存在新创建的角色
                java.util.List<org.openqa.selenium.WebElement> roles = driver.findElements(roleList);
                boolean roleFound = false;
                for (WebElement role : roles) {
                    if (role.getText().contains(roleName)) {
                        roleFound = true;
                        break;
                    }
                }
                Assertions.assertTrue(roleFound, "新创建的角色未在列表中找到");
            } else {
                // 验证失败场景
                WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'error-message')]")));
                Assertions.assertTrue(errorElement.isDisplayed(), "错误信息未显示");
            }
            
            logger.info("=== 测试用例执行完成 ===");
        } catch (Exception e) {
            logger.error("测试执行过程中出现异常: {}", e.getMessage());
            e.printStackTrace();
            Assertions.fail("测试执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试权限管理功能
     */
    @Test
    public void testPermissionManagement() {
        logger.info("=== 测试权限管理功能 ===");
        
        try {
            // 点击权限管理链接
            WebElement permLink = wait.until(ExpectedConditions.elementToBeClickable(permissionManagementLink));
            permLink.click();
            waitForPageLoad();
            
            // 验证权限管理页面元素
            WebElement permissionTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//table[@class='permission-table']")));
            Assertions.assertTrue(permissionTable.isDisplayed(), "权限管理表格未显示");
            
            logger.info("权限管理页面加载成功");
        } catch (Exception e) {
            logger.error("权限管理测试失败: {}", e.getMessage());
            e.printStackTrace();
            Assertions.fail("权限管理测试失败: " + e.getMessage());
        }
    }
}