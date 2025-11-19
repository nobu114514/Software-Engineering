package com.shop.test.selenium.A;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import io.github.bonigarcia.wdm.WebDriverManager;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminLoginTest {
    private static final Logger logger = LoggerFactory.getLogger(AdminLoginTest.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    
    // 页面URL
    private final String baseUrl = "http://localhost:8080/";
    
    // 页面元素定位器
    private final By sellerEntryLink = By.linkText("卖家入口");
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector(".btn");
    
    // 登录成功判断条件：定位"卖家后台"标题
    private final By successIndicator = By.xpath("//h1[contains(text(), '卖家后台')]");
    // 退出登录按钮
    private final By logoutButton = By.xpath("//button[contains(text(), '退出登录')]");
    
    @BeforeAll
    public void setup() {
        logger.info("=== 管理员登录测试 - 初始化开始 ===");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            js = (JavascriptExecutor) driver;
            wait = new WebDriverWait(driver, Duration.ofSeconds(20), Duration.ofMillis(500));
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            logger.info("=== 管理员登录测试 - 浏览器初始化完成 ===");
        } catch (Exception e) {
            logger.error("=== 管理员登录测试 - 初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("浏览器初始化失败，测试无法继续", e);
        }
    }
    
    @AfterAll
    public void tearDown() {
        logger.info("=== 管理员登录测试 - 清理开始 ===");
        try {
            if (driver != null) {
                driver.quit();
                logger.info("浏览器已关闭");
            }
        } catch (NoSuchSessionException e) {
            logger.warn("浏览器会话已不存在，无需重复关闭：{}", e.getMessage());
        } catch (Exception e) {
            logger.error("关闭浏览器时发生异常：{}", e.getMessage(), e);
        } finally {
            logger.info("=== 管理员登录测试 - 清理完成 ===");
        }
    }
    
    /**
     * 管理员登录测试（用例编号：18561）
     * 前置条件：系统已正常启动，管理员账户已创建
     * 步骤：1. 访问管理后台登录页面 2. 输入管理员账号：seller 3. 输入管理员密码：password 4. 点击登录按钮
     * 预期：1. 登录成功 2. 系统跳转到管理后台首页
     */
    @Test
    public void testAdminLoginSuccess() {
        logger.info("=== 开始执行管理员登录成功测试（用例编号：18561）===");
        
        try {
            // 1. 访问管理后台登录页面
            driver.get(baseUrl);
            logger.info("已访问首页：{}", baseUrl);
            
            // 点击卖家入口
            WebElement sellerEntry = wait.until(ExpectedConditions.elementToBeClickable(sellerEntryLink));
            sellerEntry.click();
            logger.info("已点击卖家入口");
            
            // 2. 输入管理员账号：seller
            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
            usernameInput.clear();
            usernameInput.sendKeys("seller");
            logger.info("已输入管理员账号：seller");
            
            // 3. 输入管理员密码：password
            WebElement passwordInput = driver.findElement(passwordField);
            passwordInput.clear();
            passwordInput.sendKeys("password");
            logger.info("已输入管理员密码：password");
            
            // 4. 点击登录按钮
            WebElement loginBtn = driver.findElement(loginButton);
            loginBtn.click();
            logger.info("已点击登录按钮");
            
            // 验证登录成功
            WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(successIndicator));
            assertTrue(successElement.isDisplayed(), "登录失败：未显示卖家后台标题");
            logger.info("登录成功，已显示卖家后台标题");
            
        } catch (Exception e) {
            logger.error("管理员登录测试失败：{}", e.getMessage(), e);
            fail("管理员登录测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 退出登录测试
     */
    @Test
    public void testLogout() {
        logger.info("=== 开始执行退出登录测试 ===");
        
        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
            logoutBtn.click();
            logger.info("已点击退出登录按钮");
            
            // 验证是否返回登录页面
            wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
            assertTrue(driver.findElement(usernameField).isDisplayed(), "退出登录失败：未返回登录页面");
            logger.info("退出登录成功，已返回登录页面");
            
        } catch (Exception e) {
            logger.error("退出登录测试失败：{}", e.getMessage(), e);
            fail("退出登录测试失败：" + e.getMessage());
        }
    }
}