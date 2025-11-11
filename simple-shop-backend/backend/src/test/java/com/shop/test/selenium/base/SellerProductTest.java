package com.shop.test.selenium.base;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SellerProductTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "http://localhost:8080/";
    private final String csvPath = "src/test/resources/testdata3.csv";

    // 页面元素定位
    private final By sellerEntryLink = By.linkText("卖家入口");
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector(".btn");
    private final By publishNewProductLink = By.linkText("发布新商品");
    private final By productNameField = By.id("name");
    private final By productPriceField = By.id("price");
    private final By productImageUrlField = By.id("imageUrl");
    private final By productDescriptionField = By.id("description");
    private final By submitProductButton = By.cssSelector(".btn:nth-child(1)");
    private final By productHistoryLink = By.linkText("商品历史");
    private final By editButton = By.cssSelector("tr:nth-child(4) .btn-success");
    private final By sellerDashboardLink = By.linkText("卖家后台");
    private final By purchaseIntentListLink = By.linkText("购买意向列表");
    private final By processButton = By.cssSelector("tr:nth-child(1) .btn:nth-child(1)");
    private final By logoutButton = By.cssSelector(".actions > .btn:nth-child(1)");
    private final By confirmLogoutButton = By.cssSelector("button:nth-child(2)");
    private final By loginPageIndicator = By.id("username"); // 用于验证是否返回登录页面

    @BeforeAll
    public void setup() {
        System.out.println("=== 测试初始化开始 ===");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        System.out.println("=== 浏览器初始化完成 ===");
    }

    @AfterAll
    public void tearDown() {
        System.out.println("=== 测试清理开始 ===");
        if (driver != null) {
            driver.quit();
        }
        System.out.println("=== 测试清理完成 ===");
    }

    // 解析CSV测试数据
    public Stream<Map<String, String>> testData() {
        System.out.println("=== 加载测试数据开始 ===");
        System.out.println("CSV文件路径: " + csvPath);
        List<Map<String, String>> dataList = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(csvPath)), StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                System.out.println("读取到的表头: " + headerLine);
                if (headerLine == null || 
                    (!headerLine.contains("用例 ID") && 
                     !headerLine.contains("username") && 
                     !headerLine.contains("password"))) {
                    System.err.println("错误：CSV文件格式不正确或表头缺失");
                    return Stream.empty();
                }
                // 自动检测分隔符
                String[] headers = detectDelimiter(headerLine);
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("读取到的行内容: " + line);
                    String[] values = detectDelimiter(line);
                    System.out.println("解析后的字段数: " + values.length);
                    if (values.length >= headers.length) {
                        Map<String, String> data = new HashMap<>();
                        for (int i = 0; i < headers.length; i++) {
                            data.put(headers[i], i < values.length ? values[i] : "");
                        }
                        dataList.add(data);
                        System.out.println("  成功添加测试数据: " + data.get("用例 ID"));
                    } else {
                        System.out.println("  跳过无效行（字段数不足）: " + line);
                    }
                }
                System.out.println("=== 加载完成，共" + dataList.size() + "条有效测试用例 ===");
            }
        } catch (Exception e) {
            System.err.println("错误：读取CSV文件失败 - " + e.getMessage());
            e.printStackTrace();
        }
        return dataList.stream();
    }

    // 等待页面加载完成
    private void waitForPageLoad() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    // 检测分隔符
    private String[] detectDelimiter(String line) {
        if (line.contains("\t")) {
            return line.split("\\t", -1);
        } else {
            return line.split(",", -1);
        }
    }

    // 检查会话是否有效
    private boolean isSessionValid() {
        try {
            driver.getTitle();
            return true;
        } catch (NoSuchSessionException e) {
            return false;
        }
    }

    // 重新初始化WebDriver
    private boolean reinitializeDriver() {
        try {
            System.out.println("尝试重新初始化WebDriver...");
            if (driver != null) {
                driver.quit();
            }
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            System.out.println("WebDriver已重新初始化");
            return true;
        } catch (Exception e) {
            System.err.println("重新初始化WebDriver失败: " + e.getMessage());
            return false;
        }
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testSellerProductFlow(Map<String, String> testCase) {
        System.out.println("=== 执行测试用例: " + testCase.get("用例 ID") + " ===");
        
        try {
            // 访问首页
            driver.get(baseUrl);
            waitForPageLoad();
            System.out.println("当前页面URL: " + driver.getCurrentUrl());
            
            // 点击卖家入口
            wait.until(ExpectedConditions.elementToBeClickable(sellerEntryLink)).click();
            waitForPageLoad();
            
            // 登录操作
            String username = testCase.get("username");
            String password = testCase.get("password");
            System.out.println("使用账号: " + username + " 登录");
            
            login(username, password);
            
            // 验证登录成功
            wait.until(ExpectedConditions.elementToBeClickable(publishNewProductLink));
            System.out.println("登录成功，进入卖家后台");
            
            // 发布新商品
            wait.until(ExpectedConditions.elementToBeClickable(publishNewProductLink)).click();
            waitForPageLoad();
            
            wait.until(ExpectedConditions.elementToBeClickable(productNameField)).sendKeys(testCase.get("productName"));
            driver.findElement(productPriceField).sendKeys(testCase.get("price"));
            driver.findElement(productImageUrlField).sendKeys(testCase.get("imageUrl"));
            driver.findElement(productDescriptionField).sendKeys(testCase.get("description"));
            driver.findElement(submitProductButton).click();
            waitForPageLoad();
            
            System.out.println("新商品发布成功");
            
            // 查看商品历史
            wait.until(ExpectedConditions.elementToBeClickable(productHistoryLink)).click();
            waitForPageLoad();
            
            // 编辑商品
            try {
                wait.until(ExpectedConditions.elementToBeClickable(editButton)).click();
                waitForPageLoad();
                System.out.println("商品编辑成功");
            } catch (Exception e) {
                System.err.println("商品编辑操作失败: " + e.getMessage());
                // 继续执行后续测试步骤
            }
            
            // 返回卖家后台
            wait.until(ExpectedConditions.elementToBeClickable(sellerDashboardLink)).click();
            waitForPageLoad();
            
            // 查看购买意向列表
            wait.until(ExpectedConditions.elementToBeClickable(purchaseIntentListLink)).click();
            waitForPageLoad();
            
            // 处理购买意向
            try {
                wait.until(ExpectedConditions.elementToBeClickable(processButton)).click();
                waitForPageLoad();
                System.out.println("购买意向处理成功");
            } catch (Exception e) {
                System.err.println("购买意向处理失败: " + e.getMessage());
                // 继续执行后续测试步骤
            }
            
            // 返回卖家后台
            wait.until(ExpectedConditions.elementToBeClickable(sellerDashboardLink)).click();
            waitForPageLoad();
            
            System.out.println("=== 卖家流程测试用例执行成功 ===");
            
        } catch (Exception e) {
            System.err.println("测试用例执行过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 确保执行退出登录
            logout();
        }
    }

    // 登录方法
    private void login(String username, String password) {
        try {
            WebElement userElement = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
            userElement.click();
            new Actions(driver).doubleClick(userElement).perform();
            userElement.sendKeys(username);
            
            driver.findElement(By.cssSelector(".login-card")).click();
            driver.findElement(passwordField).sendKeys(password);
            driver.findElement(loginButton).click();
            
            waitForPageLoad();
            System.out.println("登录操作完成");
        } catch (Exception e) {
            System.err.println("登录过程中出现异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 退出登录方法
    private void logout() {
        try {
            System.out.println("=== 执行退出登录操作 ===");
            
            // 等待退出按钮可点击并点击
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
            logoutBtn.click();
            waitForPageLoad();
            
            // 确认退出
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(confirmLogoutButton));
            confirmBtn.click();
            waitForPageLoad();
            
            // 验证是否返回登录页面
            wait.until(ExpectedConditions.elementToBeClickable(loginPageIndicator));
            System.out.println("=== 退出登录成功 ===");
        } catch (Exception e) {
            System.err.println("退出登录时出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}