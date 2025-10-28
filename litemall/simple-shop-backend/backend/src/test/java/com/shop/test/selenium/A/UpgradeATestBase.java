package com.shop.test.selenium.A;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import io.github.bonigarcia.wdm.WebDriverManager;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpgradeATestBase {
    protected static final Logger logger = LoggerFactory.getLogger(UpgradeATestBase.class);
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected final String baseUrl = "http://localhost:8080/";
    
    // 通用页面元素定位器
    protected final By adminLoginLink = By.linkText("管理员登录");
    protected final By adminUsernameField = By.id("username");
    protected final By adminPasswordField = By.id("password");
    protected final By adminLoginButton = By.cssSelector(".btn");
    protected final By logoutButton = By.linkText("退出登录");
    protected final By errorMessage = By.cssSelector("div.alert.alert-danger");
    protected final By successMessage = By.cssSelector("div.alert.alert-success");

    @BeforeAll
    public void setup() {
        logger.info("=== 升级包A测试 - 初始化开始 ===");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            js = (JavascriptExecutor) driver;
            wait = new WebDriverWait(driver, Duration.ofSeconds(20), Duration.ofMillis(500));
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            logger.info("=== 升级包A测试 - 浏览器初始化完成 ===");
        } catch (Exception e) {
            logger.error("=== 升级包A测试 - 初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("浏览器初始化失败，测试无法继续", e);
        }
    }
    
    @AfterAll
    public void tearDown() {
        logger.info("=== 升级包A测试 - 清理开始 ===");
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
            logger.info("=== 升级包A测试 - 清理完成 ===");
        }
    }
    
    // 等待页面加载完成
    protected void waitForPageLoad() {
        wait.until(d -> js.executeScript("return document.readyState").equals("complete"));
    }
    
    // 检测CSV分隔符
    protected String[] detectDelimiter(String line) {
        if (line.contains("\t")) {
            logger.debug("CSV分隔符检测为：制表符（\\t）");
            return line.split("\\t", -1);
        } else {
            logger.debug("CSV分隔符检测为：逗号（,）");
            return line.split(",", -1);
        }
    }
    
    // 管理员登录方法
    protected void adminLogin(String username, String password) {
        logger.info("执行管理员登录：{}", username);
        driver.get(baseUrl);
        waitForPageLoad();
        
        wait.until(ExpectedConditions.elementToBeClickable(adminLoginLink)).click();
        waitForPageLoad();
        
        wait.until(ExpectedConditions.elementToBeClickable(adminUsernameField)).sendKeys(username);
        wait.until(ExpectedConditions.elementToBeClickable(adminPasswordField)).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(adminLoginButton)).click();
        
        waitForPageLoad();
        logger.info("管理员登录完成");
    }
    
    // 用户登录方法
    protected void userLogin(String username, String password) {
        logger.info("执行用户登录：{}", username);
        driver.get(baseUrl + "login");
        waitForPageLoad();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("name"))).sendKeys(username);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("password"))).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit']"))).click();
        
        waitForPageLoad();
        logger.info("用户登录完成");
    }
    
    // 退出登录方法
    protected void logout() {
        try {
            logger.info("执行退出登录操作");
            wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
            waitForPageLoad();
            logger.info("退出登录成功");
        } catch (Exception e) {
            logger.warn("退出登录失败：{}", e.getMessage());
        }
    }
    
    // 通用数据加载方法
    protected Stream<Map<String, String>> loadTestData(String csvPath) {
        logger.info("加载测试数据：{}", csvPath);
        List<Map<String, String>> testDataList = new ArrayList<>();
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(csvPath)), StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            
            try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    logger.error("CSV文件为空");
                    return Stream.empty();
                }
                
                String[] headers = detectDelimiter(headerLine);
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] values = detectDelimiter(line);
                    Map<String, String> testCase = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        testCase.put(headers[i].trim(), values[i].trim());
                    }
                    testDataList.add(testCase);
                }
            }
            
            logger.info("成功加载{}条测试数据", testDataList.size());
        } catch (Exception e) {
            logger.error("加载测试数据失败：{}", e.getMessage(), e);
        }
        
        return testDataList.stream();
    }
    
    // 安全的元素查找方法
    protected WebElement findElementSafely(By locator, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return shortWait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            logger.warn("元素查找失败：{} - {}", locator, e.getMessage());
            return null;
        }
    }
    
    // 截图方法
    protected void takeScreenshot(String testName) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File source = screenshot.getScreenshotAs(OutputType.FILE);
            File destination = new File("screenshots/" + testName + "_" + System.currentTimeMillis() + ".png");
            Files.createDirectories(destination.getParentFile().toPath());
            Files.copy(source.toPath(), destination.toPath());
            logger.info("已保存截图：{}", destination.getAbsolutePath());
        } catch (Exception e) {
            logger.error("截图失败：{}", e.getMessage());
        }
    }
}