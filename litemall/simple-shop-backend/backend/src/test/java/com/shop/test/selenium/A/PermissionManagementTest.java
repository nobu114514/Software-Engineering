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
 * A升级包 - 权限管理测试类
 * 测试权限的分配、修改和验证功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PermissionManagementTest extends DataDrivenTestBase {
    // 管理员登录信息
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    
    // 页面元素定位
    private final By permissionManagementMenu = By.xpath("//a[contains(@href, 'permission') or contains(text(), '权限管理')]");
    private final By roleSelectDropdown = By.xpath("//select[contains(@class, 'role-select') or @id='role-select']");
    private final By roleOption = By.xpath("//select[contains(@class, 'role-select') or @id='role-select']/option");
    private final By permissionTreeItem = By.xpath("//div[contains(@class, 'permission-tree')]/div");
    private final By checkboxPermission = By.xpath("//input[@type='checkbox' and @name='permission']");
    private final By savePermissionButton = By.xpath("//button[contains(text(), '保存权限') or @id='save-permissions']");
    private final By successNotification = By.xpath("//div[contains(@class, 'success') and contains(@class, 'notification')]");
    private final By functionModuleCheckbox = By.xpath("//input[@type='checkbox' and @data-type='module']");
    private final By operationCheckbox = By.xpath("//input[@type='checkbox' and @data-type='operation']");
    
    @BeforeEach
    public void setup() {
        logger.info("=== 权限管理测试前准备 ===");
        // 登录管理员账号
        boolean loggedIn = LoginUtils.login(driver, ADMIN_USERNAME, ADMIN_PASSWORD);
        Assertions.assertTrue(loggedIn, "管理员登录失败");
        
        // 导航到权限管理页面
        navigateToPermissionManagement();
    }
    
    @AfterEach
    public void teardown() {
        logger.info("=== 权限管理测试后清理 ===");
        // 退出登录
        LoginUtils.logout(driver);
    }
    
    /**
     * 导航到权限管理页面
     */
    private void navigateToPermissionManagement() {
        try {
            // 尝试通过菜单导航
            WebElement permMenu = wait.until(ExpectedConditions.elementToBeClickable(permissionManagementMenu));
            permMenu.click();
            waitForPageLoad();
        } catch (Exception e) {
            // 如果菜单无法点击，直接导航到权限管理URL
            logger.warn("通过菜单导航失败，尝试直接导航到权限管理页面");
            navigateTo("/admin/permission/list");
        }
    }
    
    /**
     * 加载权限测试数据
     */
    public Stream<Map<String, String>> permissionTestData() {
        String csvPath = "src/test/resources/permission_management_test_data.csv";
        return loadTestData(csvPath);
    }
    
    /**
     * 测试角色权限分配功能
     */
    @ParameterizedTest
    @MethodSource("permissionTestData")
    public void testAssignPermissions(Map<String, String> testData) {
        String testId = testData.get("用例 ID");
        String roleName = testData.get("角色名称");
        String modules = testData.get("功能模块");
        String expectedResult = testData.get("预期结果");
        
        logger.info("=== 执行测试用例: {} - 权限分配 ===", testId);
        logger.info("角色: {}, 模块: {}", roleName, modules);
        
        try {
            // 选择角色
            selectRole(roleName);
            
            // 分配权限
            if (modules != null && !modules.isEmpty()) {
                String[] moduleArray = modules.split(",");
                for (String module : moduleArray) {
                    module = module.trim();
                    assignModulePermission(module);
                }
            }
            
            // 保存权限
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(savePermissionButton));
            saveBtn.click();
            
            // 验证结果
            if ("成功".equals(expectedResult)) {
                // 验证成功提示
                WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successNotification));
                Assertions.assertTrue(success.isDisplayed(), "权限保存成功提示未显示");
                
                // 重新加载页面并验证权限是否正确保存
                driver.navigate().refresh();
                waitForPageLoad();
                selectRole(roleName);
                
                // 验证权限是否正确保存
                boolean allModulesAssigned = true;
                if (modules != null && !modules.isEmpty()) {
                    String[] moduleArray = modules.split(",");
                    for (String module : moduleArray) {
                        module = module.trim();
                        if (!isModulePermissionAssigned(module)) {
                            allModulesAssigned = false;
                            break;
                        }
                    }
                }
                Assertions.assertTrue(allModulesAssigned, "权限未正确保存");
                logger.info("权限分配测试成功");
            } else {
                // 验证失败场景
                WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'error')]")
                ));
                Assertions.assertTrue(errorElement.isDisplayed(), "错误提示未显示");
                logger.info("预期的错误场景验证成功");
            }
        } catch (Exception e) {
            logger.error("权限分配测试失败: {}", e.getMessage());
            e.printStackTrace();
            Assertions.fail("权限分配测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试权限树层级关系
     */
    @Test
    public void testPermissionHierarchy() {
        logger.info("=== 测试权限树层级关系 ===");
        
        try {
            // 选择第一个可用角色
            List<WebElement> options = driver.findElements(roleOption);
            if (options.size() > 1) { // 跳过默认选项
                WebElement roleElement = options.get(1);
                String roleName = roleElement.getText();
                logger.info("选择测试角色: {}", roleName);
                roleElement.click();
                
                // 找到第一个功能模块复选框
                List<WebElement> moduleCheckboxes = driver.findElements(functionModuleCheckbox);
                if (!moduleCheckboxes.isEmpty()) {
                    WebElement moduleCheckbox = moduleCheckboxes.get(0);
                    String moduleName = moduleCheckbox.getAttribute("data-name");
                    logger.info("测试模块: {}", moduleName);
                    
                    // 点击模块复选框
                    moduleCheckbox.click();
                    
                    // 验证子操作权限是否自动选中
                    List<WebElement> operationCheckboxes = driver.findElements(
                            By.xpath("//div[contains(@class, 'permission-group') and contains(text(), '" + moduleName + "')]//" + operationCheckbox.toString().substring(operationCheckbox.toString().indexOf("xpath: ") + 7))
                    );
                    
                    boolean allChecked = true;
                    for (WebElement opCheckbox : operationCheckboxes) {
                        if (!opCheckbox.isSelected()) {
                            allChecked = false;
                            break;
                        }
                    }
                    
                    Assertions.assertTrue(allChecked, "父模块选中后，子操作权限未自动选中");
                    logger.info("权限树层级关系测试成功");
                } else {
                    logger.warn("未找到功能模块复选框");
                }
            } else {
                logger.warn("未找到可测试的角色");
            }
        } catch (Exception e) {
            logger.error("权限树层级测试失败: {}", e.getMessage());
            e.printStackTrace();
            Assertions.fail("权限树层级测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 选择角色
     */
    private void selectRole(String roleName) {
        try {
            WebElement roleDropdown = wait.until(ExpectedConditions.elementToBeClickable(roleSelectDropdown));
            roleDropdown.click();
            
            List<WebElement> options = driver.findElements(roleOption);
            for (WebElement option : options) {
                if (option.getText().equals(roleName)) {
                    option.click();
                    logger.info("成功选择角色: {}", roleName);
                    waitForPageLoad();
                    return;
                }
            }
            logger.warn("未找到指定角色: {}", roleName);
        } catch (Exception e) {
            logger.error("选择角色失败: {}", e.getMessage());
        }
    }
    
    /**
     * 分配模块权限
     */
    private void assignModulePermission(String moduleName) {
        try {
            WebElement moduleCheckbox = driver.findElement(
                    By.xpath("//input[@type='checkbox' and @data-name='" + moduleName + "']")
            );
            if (!moduleCheckbox.isSelected()) {
                moduleCheckbox.click();
                logger.info("已分配模块权限: {}", moduleName);
            }
        } catch (Exception e) {
            logger.error("分配模块权限失败: {}", e.getMessage());
        }
    }
    
    /**
     * 检查模块权限是否已分配
     */
    private boolean isModulePermissionAssigned(String moduleName) {
        try {
            WebElement moduleCheckbox = driver.findElement(
                    By.xpath("//input[@type='checkbox' and @data-name='" + moduleName + "']")
            );
            return moduleCheckbox.isSelected();
        } catch (Exception e) {
            logger.error("检查模块权限失败: {}", e.getMessage());
            return false;
        }
    }
}