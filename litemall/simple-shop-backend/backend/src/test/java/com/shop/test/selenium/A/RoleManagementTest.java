package com.shop.test.selenium.A;

import com.shop.test.selenium.DataDrivenTestBase;
import com.shop.test.selenium.LoginUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A升级包 - 角色管理测试类
 * 测试角色的增删改查功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoleManagementTest extends DataDrivenTestBase {
    // 管理员登录信息
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    
    // 页面元素定位
    private final By roleManagementMenu = By.xpath("//a[contains(@href, 'role') or contains(text(), '角色管理')]");
    private final By addRoleButton = By.xpath("//button[contains(text(), '新增角色') or @id='add-role']");
    private final By roleNameInput = By.xpath("//input[@name='roleName' or @id='role-name']");
    private final By roleDescTextarea = By.xpath("//textarea[@name='description' or @id='role-desc']");
    private final By permissionCheckbox = By.xpath("//input[@type='checkbox' and contains(@class, 'permission')]");
    private final By submitButton = By.xpath("//button[contains(text(), '保存') or @id='submit-btn']");
    private final By cancelButton = By.xpath("//button[contains(text(), '取消') or @id='cancel-btn']");
    private final By deleteRoleButton = By.xpath("//button[contains(text(), '删除') or @class='delete-role']");
    private final By confirmDeleteButton = By.xpath("//button[contains(text(), '确认删除')]");
    private final By successMessage = By.xpath("//div[contains(@class, 'success') or contains(@class, 'toast-success')]");
    private final By errorMessage = By.xpath("//div[contains(@class, 'error') or contains(@class, 'toast-error')]");
    private final By roleTable = By.xpath("//table[contains(@class, 'role-table')]/tbody/tr");
    
    @BeforeEach
    public void setup() {
        logger.info("=== 测试前准备 ===");
        // 登录管理员账号
        boolean loggedIn = LoginUtils.login(driver, ADMIN_USERNAME, ADMIN_PASSWORD);
        Assertions.assertTrue(loggedIn, "管理员登录失败");
        
        // 导航到角色管理页面
        navigateToRoleManagement();
    }
    
    @AfterEach
    public void teardown() {
        logger.info("=== 测试后清理 ===");
        // 退出登录
        LoginUtils.logout(driver);
    }
    
    /**
     * 导航到角色管理页面
     */
    private void navigateToRoleManagement() {
        try {
            // 尝试通过菜单导航
            WebElement roleMenu = wait.until(ExpectedConditions.elementToBeClickable(roleManagementMenu));
            roleMenu.click();
            waitForPageLoad();
        } catch (Exception e) {
            // 如果菜单无法点击，直接导航到角色管理URL
            logger.warn("通过菜单导航失败，尝试直接导航到角色管理页面");
            navigateTo("/admin/role/list");
        }
    }
    
    /**
     * 加载测试数据
     */
    public Stream<Map<String, String>> roleTestData() {
        // 这里可以从CSV文件加载数据
        String csvPath = "src/test/resources/role_management_test_data.csv";
        return loadTestData(csvPath);
    }
    
    /**
     * 测试添加角色功能
     */
    @ParameterizedTest
    @MethodSource("roleTestData")
    public void testAddRole(Map<String, String> testData) {
        String testId = testData.get("用例 ID");
        String roleName = testData.get("角色名称");
        String description = testData.get("角色描述");
        String permissions = testData.get("权限设置");
        String expectedResult = testData.get("预期结果");
        
        logger.info("=== 执行测试用例: {} - 添加角色 ===", testId);
        logger.info("角色名称: {}, 描述: {}", roleName, description);
        
        try {
            // 点击新增角色按钮
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addRoleButton));
            addBtn.click();
            
            // 输入角色信息
            wait.until(ExpectedConditions.visibilityOfElementLocated(roleNameInput)).sendKeys(roleName);
            wait.until(ExpectedConditions.visibilityOfElementLocated(roleDescTextarea)).sendKeys(description);
            
            // 设置权限（如果有）
            if (permissions != null && !permissions.isEmpty()) {
                List<WebElement> checkboxes = driver.findElements(permissionCheckbox);
                for (WebElement checkbox : checkboxes) {
                    String permissionText = checkbox.getAttribute("data-permission");
                    if (permissionText != null && permissions.contains(permissionText)) {
                        if (!checkbox.isSelected()) {
                            checkbox.click();
                        }
                    }
                }
            }
            
            // 保存角色
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            saveBtn.click();
            
            // 验证结果
            if ("成功".equals(expectedResult)) {
                // 验证成功提示
                WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
                Assertions.assertTrue(successMsg.isDisplayed(), "成功提示未显示");
                
                // 验证角色是否出现在列表中
                boolean roleExists = checkRoleExistsInList(roleName);
                Assertions.assertTrue(roleExists, "新增角色未在列表中找到");
                logger.info("角色添加成功: {}", roleName);
            } else {
                // 验证错误提示
                WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
                Assertions.assertTrue(errorMsg.isDisplayed(), "错误提示未显示");
                logger.info("预期的错误场景验证成功");
            }
            
            // 如果打开了弹窗，关闭它
            try {
                WebElement cancelBtn = driver.findElement(cancelButton);
                if (cancelBtn.isDisplayed()) {
                    cancelBtn.click();
                }
            } catch (Exception e) {
                // 忽略，可能弹窗已经关闭
            }
        } catch (Exception e) {
            logger.error("测试执行失败: {}", e.getMessage());
            e.printStackTrace();
            Assertions.fail("测试用例执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试删除角色功能
     */
    @Test
    public void testDeleteRole() {
        logger.info("=== 测试删除角色功能 ===");
        
        try {
            // 先添加一个测试角色
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addRoleButton));
            addBtn.click();
            
            String testRoleName = "测试角色_delete";
            wait.until(ExpectedConditions.visibilityOfElementLocated(roleNameInput)).sendKeys(testRoleName);
            wait.until(ExpectedConditions.visibilityOfElementLocated(roleDescTextarea)).sendKeys("用于测试删除功能的角色");
            
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            saveBtn.click();
            
            // 等待保存成功
            wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
            waitForPageLoad();
            
            // 查找并删除角色
            List<WebElement> roles = driver.findElements(roleTable);
            for (WebElement role : roles) {
                if (role.getText().contains(testRoleName)) {
                    WebElement deleteBtn = role.findElement(deleteRoleButton);
                    deleteBtn.click();
                    
                    // 确认删除
                    WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteButton));
                    confirmBtn.click();
                    
                    // 验证删除成功
                    wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
                    break;
                }
            }
            
            // 验证角色已被删除
            boolean roleExists = checkRoleExistsInList(testRoleName);
            Assertions.assertFalse(roleExists, "角色未被成功删除");
            logger.info("角色删除测试成功");
        } catch (Exception e) {
            logger.error("删除角色测试失败: {}", e.getMessage());
            e.printStackTrace();
            Assertions.fail("删除角色测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查角色是否存在于列表中
     */
    private boolean checkRoleExistsInList(String roleName) {
        try {
            List<WebElement> rows = driver.findElements(roleTable);
            for (WebElement row : rows) {
                if (row.getText().contains(roleName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("检查角色列表失败: {}", e.getMessage());
        }
        return false;
    }
}