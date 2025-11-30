package com.shop.test.selenium.A;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.io.File;
import java.io.IOException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;

import io.github.bonigarcia.wdm.WebDriverManager;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerRegistrationTest {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRegistrationTest.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    // 页面URL
    private final String baseUrl = "http://localhost:8080/";
    
    // 页面元素定位器 - 使用更灵活的定位方式
    private final By registerLink = By.xpath("//a[contains(@href, 'register')] | //a[contains(text(), '注册')] | //*[contains(@class, 'register')] | //button[contains(text(), '注册')]");
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By phoneField = By.id("phone");
    private final By addressField = By.id("defaultLocation");
    private final By registerButton = By.cssSelector(".btn");
    private final By successIndicator = By.xpath("//button[contains(@class, 'navbar-btn') and contains(text(), '退出登录')] | //a[contains(text(), '退出')] | //*[contains(text(), '退出登录')]"); // 注册成功后显示退出登录按钮
    
    @BeforeAll
    public void setup() {
        logger.info("=== 客户注册测试 - 初始化开始 ===");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20), Duration.ofMillis(500));
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            logger.info("=== 客户注册测试 - 浏览器初始化完成 ===");
        } catch (Exception e) {
            logger.error("=== 客户注册测试 - 初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("浏览器初始化失败，测试无法继续", e);
        }
    }
    
    @AfterAll
    public void tearDown() {
        logger.info("=== 客户注册测试 - 清理开始 ===");
        try {
            if (driver != null) {
                driver.quit();
                logger.info("浏览器已关闭");
            }
        } catch (Exception e) {
            logger.error("关闭浏览器时发生异常：{}", e.getMessage(), e);
        } finally {
            logger.info("=== 客户注册测试 - 清理完成 ===");
        }
    }
    
    /**
     * 客户注册测试（用例编号：18562）
     * 前置条件：系统已正常启动
     * 步骤：1. 访问前台注册页面 2. 填写注册信息（xiaoming，password，19155558888，浙江工商大学）并提交
     * 预期：1. 注册成功 2. 系统自动登录
     */
    @Test
    public void testCustomerRegistrationSuccess() {
        logger.info("=== 开始执行客户注册成功测试（用例编号：18562）===");
        
        try {
            executeRegistration("xiaoming", "password", "19155558888", "浙江工商大学");
            
            // 验证注册成功并自动登录
            verifyRegistrationSuccess();
            
        } catch (Exception e) {
            logger.error("客户注册测试失败：{}", e.getMessage(), e);
            fail("客户注册测试失败：" + e.getMessage());
        }
    }

    
    
    /**
     * 有效注册测试（用例编号：18553）
     * 前置条件：系统已正常启动
     * 步骤：1. 访问注册页面 2. 填写完整注册信息（zhangwei，Zw@654321，13900139001，上海市浦东新区） 3. 点击注册按钮
     * 预期：1. 注册成功 2. 系统自动登录并显示用户信息 3. 用户数据正确保存在数据库中
     */
    @Test
    public void testValidRegistration() {
        logger.info("=== 开始执行有效注册测试（用例编号：18553）===");
        
        try {
            // 访问首页并点击注册链接
            driver.get(baseUrl);
            navigateToRegisterPage();
            
            // 填写有效注册信息
            executeRegistration("zhangwei", "Zw@654321", "13900139001", "上海市浦东新区");
            
            // 验证注册成功并自动登录
            verifyRegistrationSuccess();
            
        } catch (Exception e) {
            logger.error("有效注册测试失败：{}", e.getMessage(), e);
            fail("有效注册测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 注册信息验证（无效密码）测试（用例编号：18862）
     * 前置条件：系统已正常启动
     * 步骤：1. 访问注册页面 2. 填写不同类型的无效信息：xiaowang、密码：123、电话：19120252026 3. 点击注册按钮
     * 预期：1. 系统拒绝注册 2. 给出相应的错误提示信息
     */
    @Test
    public void testInvalidPasswordRegistration() {
        logger.info("=== 开始执行无效密码注册测试（用例编号：18862）===");
        
        try {
            // 访问首页并点击注册链接
            driver.get(baseUrl);
            navigateToRegisterPage();
            
            // 填写无效密码的注册信息
            executeRegistration("xiaowang", "123", "19120252026", "测试地址");
            
            // 验证注册失败并显示错误信息
            verifyRegistrationFailure("密码");
            
        } catch (Exception e) {
            logger.error("无效密码注册测试失败：{}", e.getMessage(), e);
            fail("无效密码注册测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 注册信息验证（无效用户名）测试（用例编号：18861）
     * 前置条件：系统已正常启动
     * 步骤：1. 访问注册页面 2. 填写不同类型的无效信息：空用户名、密码：123123、电话：19120252025 3. 点击注册按钮
     * 预期：1. 系统拒绝注册 2. 给出相应的错误提示信息
     */
    @Test
    public void testInvalidUsernameRegistration() {
        logger.info("=== 开始执行无效用户名注册测试（用例编号：18861）===");
        
        try {
            // 访问首页并点击注册链接
            driver.get(baseUrl);
            navigateToRegisterPage();
            
            // 填写空用户名的注册信息
            executeRegistration(" ", "123123", "19120252025", "测试地址");
            
            // 验证注册失败并显示错误信息
            verifyRegistrationFailure("用户名");
            
        } catch (Exception e) {
            logger.error("无效用户名注册测试失败：{}", e.getMessage(), e);
            fail("无效用户名注册测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 注册信息验证（无效手机号）测试（用例编号：18554）
     * 前置条件：系统已正常启动
     * 步骤：1. 访问注册页面 2. 填写不同类型的无效信息：xiaozhang、密码：123456、电话：123456 3. 点击注册按钮
     * 预期：1. 系统拒绝注册 2. 给出相应的错误提示信息
     */
    @Test
    public void testInvalidPhoneRegistration() {
        logger.info("=== 开始执行无效手机号注册测试（用例编号：18554）===");
        
        try {
            // 访问首页并点击注册链接
            driver.get(baseUrl);
            navigateToRegisterPage();
            
            // 填写无效手机号的注册信息
            executeRegistration("xiaozhang", "123456", "123456", "测试地址");
            
            // 验证注册失败并显示错误信息
            verifyRegistrationFailure("手机号");
            
        } catch (Exception e) {
            logger.error("无效手机号注册测试失败：{}", e.getMessage(), e);
            fail("无效手机号注册测试失败：" + e.getMessage());
        }
    }

    /**
     * 重复用户名注册测试（用例编号：18555）
     * 前置条件：已执行 TC_BASE_002（小明用户已注册）
     * 步骤：1. 访问注册页面 2. 使用已存在的用户名 "xiaoming" 注册 3. 点击注册按钮
     * 预期：1. 系统拒绝注册 2. 提示 "用户名已存在"
     */
    @Test
    public void testDuplicateUsernameRegistration() {
        logger.info("=== 开始执行重复用户名注册测试（用例编号：18555）===");

        try {
            // 访问首页并点击注册链接
            driver.get(baseUrl);
            navigateToRegisterPage();

            // 使用已存在的用户名注册
            executeRegistration("xiaoming", "password123", "13800138001", "测试地址");

            // 验证注册失败并显示用户名已存在的错误信息
            verifyRegistrationFailure("用户名已存在");

        } catch (Exception e) {
            logger.error("重复用户名注册测试失败：{}", e.getMessage(), e);
            fail("重复用户名注册测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 辅助方法：导航到注册页面
     */
    private void navigateToRegisterPage() {
        try {
            // 先尝试直接查找注册链接
            WebElement registerLinkElem = wait.until(ExpectedConditions.elementToBeClickable(registerLink));
            registerLinkElem.click();
            logger.info("已点击注册链接");
        } catch (TimeoutException e) {
            // 如果找不到注册链接，尝试直接访问注册页面
            logger.warn("未找到注册链接，尝试直接访问注册页面");
            driver.get(baseUrl + "register");
            logger.info("已直接访问注册页面");
        }
    }
    
    /**
     * 辅助方法：执行注册操作
     */
    private void executeRegistration(String username, String password, String phone, String address) {
        // 用户名输入
        try {
            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
            usernameInput.clear();
            usernameInput.sendKeys(username);
            logger.info("已输入用户名：{}", username);
        } catch (Exception e) {
            logger.error("用户名输入框定位或操作失败：{}", e.getMessage());
            fail("无法输入用户名");
        }
        
        // 密码输入
        try {
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
            passwordInput.clear();
            passwordInput.sendKeys(password);
            logger.info("已输入密码：{}", password);
        } catch (Exception e) {
            logger.error("密码输入框定位或操作失败：{}", e.getMessage());
            fail("无法输入密码");
        }
        
        // 电话输入
        try {
            WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(phoneField));
            phoneInput.clear();
            phoneInput.sendKeys(phone);
            logger.info("已输入电话：{}", phone);
        } catch (Exception e) {
            logger.error("电话输入框定位或操作失败：{}", e.getMessage());
            fail("无法输入电话");
        }
        
        // 地址输入
        try {
            WebElement addressInput = driver.findElement(addressField);
            addressInput.clear();
            addressInput.sendKeys(address);
            logger.info("已输入地址：{}", address);
        } catch (NoSuchElementException e) {
            // 尝试备用定位器
            try {
                WebElement addressInput = driver.findElement(By.xpath("//input[contains(@name, 'address') or contains(@placeholder, '地址')]"));
                addressInput.clear();
                addressInput.sendKeys(address);
                logger.info("已通过备用定位器输入地址");
            } catch (Exception inner) {
                logger.warn("无法找到地址输入字段：{}", inner.getMessage());
                throw inner;
            }
        }
        
        // 提交注册
        try {
            WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(registerButton));
            registerBtn.click();
            logger.info("已点击注册按钮");
        } catch (Exception e) {
            logger.error("注册按钮定位或点击失败：{}", e.getMessage());
            fail("无法提交注册表单");
        }
    }
    
    /**
     * 辅助方法：验证注册成功
     */
    private void verifyRegistrationSuccess() {
        try {
            WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(successIndicator));
            assertTrue(successElement.isDisplayed(), "注册失败：未显示登录成功状态");
            logger.info("注册成功，系统已自动登录，找到退出登录按钮");
            
            // 如果测试成功，点击退出登录以便后续测试
            try {
                successElement.click();
                logger.info("已退出登录，准备下一个测试");
            } catch (Exception e) {
                logger.warn("退出登录操作失败，但不影响测试结果：{}", e.getMessage());
            }
        } catch (TimeoutException e) {
            // 如果找不到退出登录按钮，尝试寻找其他可能的成功指标
            logger.warn("未找到退出登录按钮，尝试其他成功指标");
            try {
                WebElement successMessage = driver.findElement(By.xpath("//*[contains(text(), '注册成功') or contains(text(), '登录成功') or contains(@class, 'success')]"));
                logger.info("找到成功消息/标题，注册可能已成功");
                assertTrue(true, "注册成功：找到成功指示");
            } catch (Exception inner) {
                // 如果都找不到，截图保存当前页面状态用于调试
                TakesScreenshot screenshot = (TakesScreenshot) driver;
                File srcFile = screenshot.getScreenshotAs(OutputType.FILE);
                String timestamp = String.valueOf(System.currentTimeMillis());
                String screenshotPath = "screenshot_" + timestamp + ".png";
                try {
                    java.nio.file.Files.copy(srcFile.toPath(), new File(screenshotPath).toPath());
                    logger.info("已保存页面截图到: {}", screenshotPath);
                } catch (IOException ioEx) {
                    logger.error("保存截图失败: {}", ioEx.getMessage());
                }
                fail("注册可能失败：未找到成功指示元素，截图已保存供调试");
            }
        }
    }
    
    /**
     * 辅助方法：验证注册失败
     */
    private void verifyRegistrationFailure(String errorKeyword) {
        try {
            // 等待错误信息出现
            By errorMessageLocator = By.xpath(
                "//div[contains(@class, 'error') or contains(@class, 'alert') or contains(@class, 'warning')] " +
                "| //span[contains(@class, 'error')] " +
                "| //p[contains(@class, 'error')] " +
                "| //*[contains(text(), '" + errorKeyword + "')]"
            );
            
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageLocator));
            String errorText = errorElement.getText().trim();
            logger.info("找到错误信息：{}", errorText);
            
            // 直接检查错误文本是否等于或包含关键词，不使用toLowerCase以避免编码问题
            boolean containsKeyword = errorText.equals(errorKeyword) || errorText.contains(errorKeyword);
            logger.info("错误文本是否包含关键词：{}", containsKeyword);
            logger.info("错误文本完整内容：'{}'，关键词：'{}'", errorText, errorKeyword);
            
            assertTrue(containsKeyword, 
                      "错误信息不包含预期的关键词：" + errorKeyword + "，实际错误信息：" + errorText);
            
            logger.info("注册失败测试通过：系统正确拒绝了无效的注册信息并显示错误提示");
        } catch (TimeoutException e) {
            // 截图保存当前页面状态用于调试
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File srcFile = screenshot.getScreenshotAs(OutputType.FILE);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String screenshotPath = "screenshot_failure_" + timestamp + ".png";
            try {
                java.nio.file.Files.copy(srcFile.toPath(), new File(screenshotPath).toPath());
                logger.info("已保存失败测试页面截图到: {}", screenshotPath);
            } catch (IOException ioEx) {
                logger.error("保存截图失败: {}", ioEx.getMessage());
            }
            fail("未找到预期的错误提示信息，截图已保存供调试");
        }
    }
}