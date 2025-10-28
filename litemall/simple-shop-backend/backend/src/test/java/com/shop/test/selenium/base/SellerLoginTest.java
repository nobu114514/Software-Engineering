package com.shop.test.selenium.base;

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
public class SellerLoginTest {
    private static final Logger logger = LoggerFactory.getLogger(SellerLoginTest.class);
    
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
    
    // 登录成功判断条件：定位“卖家后台”标题
    private final By successIndicator = By.xpath("//h1[contains(text(), '卖家后台')]");
    // 登录失败判断条件
    private final By errorMessage = By.cssSelector("div.alert.alert-danger[data-v-02360c40='']");
    // 退出登录按钮
    private final By logoutButton = By.xpath("//button[contains(text(), '退出登录')]");
    
    @BeforeAll
    public void setup() {
        logger.info("=== 卖家登录测试 - 初始化开始 ===");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            js = (JavascriptExecutor) driver;
            wait = new WebDriverWait(driver, Duration.ofSeconds(20), Duration.ofMillis(500));
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            logger.info("=== 卖家登录测试 - 浏览器初始化完成 ===");
        } catch (Exception e) {
            logger.error("=== 卖家登录测试 - 初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("浏览器初始化失败，测试无法继续", e);
        }
    }
    
    @AfterAll
    public void tearDown() {
        logger.info("=== 卖家登录测试 - 清理开始 ===");
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
            logger.info("=== 卖家登录测试 - 清理完成 ===");
        }
    }
    
    // CSV数据文件路径
    private final String csvPath = "src/test/resources/testdata.csv";
    
    public Stream<Map<String, String>> sellerTestData() {
        logger.info("=== 加载卖家登录测试数据 - 开始 ===");
        logger.info("CSV数据文件路径：{}", csvPath);
        
        // 检查文件是否存在
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            logger.error("严重错误：CSV文件不存在！路径：{}", csvFile.getAbsolutePath());
            return Stream.empty();
        }
        if (!csvFile.isFile()) {
            logger.error("严重错误：路径指向的不是文件！路径：{}", csvFile.getAbsolutePath());
            return Stream.empty();
        }
        
        List<Map<String, String>> testDataList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(Paths.get(csvPath)), StandardCharsets.UTF_8))) {
            
            String headerLine = reader.readLine();
            if (headerLine == null) {
                logger.error("CSV文件为空，无测试数据");
                return Stream.empty();
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            logger.info("读取到CSV表头：{}", headerLine);
            
            String[] headers = detectDelimiter(headerLine);
            if (!isValidHeader(headers)) {
                logger.error("CSV表头格式不正确，需包含：用例ID、用户名、密码、预期结果");
                return Stream.empty();
            }
            
            String dataLine;
            int lineNum = 2;
            while ((dataLine = reader.readLine()) != null) {
                if (dataLine.trim().isEmpty()) {
                    logger.warn("跳过空行（行号：{}）", lineNum);
                    lineNum++;
                    continue;
                }
                
                logger.info("读取到数据行（行号：{}）：{}", lineNum, dataLine);
                String[] dataValues = detectDelimiter(dataLine);
                
                if (dataValues.length < headers.length) {
                    logger.warn("跳过无效数据行（行号：{}）- 字段数不足（预期：{}，实际：{}）",
                            lineNum, headers.length, dataValues.length);
                    lineNum++;
                    continue;
                }
                
                Map<String, String> testCase = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String value = (i < dataValues.length) ? dataValues[i].trim() : "";
                    testCase.put(headers[i], "null".equalsIgnoreCase(value) ? "" : value);
                }
                
                String caseId = testCase.get("用例ID");
                String username = testCase.get("用户名");
                String expectedResult = testCase.get("预期结果");
                logger.info("解析测试用例（ID：{}）- 用户名：{}，预期结果：{}", caseId, username, expectedResult);
                
                testDataList.add(testCase);
                lineNum++;
            }
            
            logger.info("=== 加载卖家登录测试数据 - 完成，共加载{}条有效用例 ===", testDataList.size());
        } catch (IOException e) {
            logger.error("读取CSV文件时发生IO异常：{}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("解析CSV测试数据时发生异常：{}", e.getMessage(), e);
        }
        
        return testDataList.stream();
    }
    
    @ParameterizedTest
    @MethodSource("sellerTestData")
    public void testSellerLoginFunctionality(Map<String, String> testCase) {
        String caseId = testCase.get("用例ID");
        String username = testCase.get("用户名");
        String password = testCase.get("密码");
        String expectedResult = testCase.get("预期结果");
        String expectedTip = testCase.get("预期提示");
        
        logger.info("=== 执行卖家登录测试用例（ID：{}）- 开始 ===", caseId);
        logger.info("用例详情：用户名={}，密码={}，预期结果={}，预期提示={}",
                username, (password == null || password.isEmpty()) ? "空" : "******", expectedResult, expectedTip);
        
        try {
            accessSellerEntryPage();
            inputUsername(username);
            inputPassword(password);
            submitLoginForm();
            
            if ("登录成功".equalsIgnoreCase(expectedResult)) {
                verifyLoginSuccess(expectedTip);
                logoutSeller();
            } else {
                verifyLoginFailure(expectedTip);
            }
            
            logger.info("=== 执行卖家登录测试用例（ID：{}）- 成功 ===", caseId);
        } catch (AssertionError e) {
            logger.error("=== 执行卖家登录测试用例（ID：{}）- 断言失败：{}", caseId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("=== 执行卖家登录测试用例（ID：{}）- 执行异常：{}", caseId, e.getMessage(), e);
            throw new RuntimeException("用例执行异常", e);
        } finally {
            logger.info("=== 执行卖家登录测试用例（ID：{}）- 结束 ===", caseId);
        }
    }
    
    private void accessSellerEntryPage() {
        logger.info("访问卖家入口页面：{}", baseUrl);
        driver.get(baseUrl);
        
        wait.until(d -> js.executeScript("return document.readyState").equals("complete"));
        logger.info("首页加载完成，当前URL：{}", driver.getCurrentUrl());
        
        WebElement sellerEntry = wait.until(ExpectedConditions.elementToBeClickable(sellerEntryLink));
        sellerEntry.click();
        logger.info("点击'卖家入口'链接，跳转后URL：{}", driver.getCurrentUrl());
        
        wait.until(d -> js.executeScript("return document.readyState").equals("complete"));
    }
    
    private void inputUsername(String username) {
        logger.info("输入用户名：{}", username == null || username.isEmpty() ? "空" : username);
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
        
        Actions actions = new Actions(driver);
        actions.doubleClick(usernameInput).perform();
        usernameInput.sendKeys(Keys.BACK_SPACE);
        
        if (username != null && !username.isEmpty()) {
            usernameInput.sendKeys(username);
        }
        
        assertEquals(username == null ? "" : username, usernameInput.getAttribute("value"), "用户名输入错误");
        logger.info("用户名输入完成");
    }
    
    private void inputPassword(String password) {
        logger.info("输入密码：{}", password == null || password.isEmpty() ? "空" : "******");
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        
        passwordInput.clear();
        if (password != null && !password.isEmpty()) {
            passwordInput.sendKeys(password);
        }
        
        logger.info("密码输入完成");
    }
    
    private void submitLoginForm() {
        logger.info("提交登录表单（点击登录按钮）");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginBtn.click();
        
        wait.until(d -> !js.executeScript("return document.readyState").equals("loading"));
        logger.info("登录表单提交完成，当前URL：{}", driver.getCurrentUrl());
    }
    
    private void verifyLoginSuccess(String expectedTip) {
        logger.info("验证登录成功场景，预期提示：{}", expectedTip);
        
        // 验证成功标识元素显示
        WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(successIndicator));
        assertTrue(successElement.isDisplayed(), "登录成功标识元素未显示");
        
        // 验证成功提示文本
        String actualText = successElement.getText().trim();
        if (expectedTip != null && !expectedTip.isEmpty()) {
            assertTrue(actualText.contains(expectedTip),
                    String.format("登录成功提示不符合预期：实际=%s，预期包含=%s", actualText, expectedTip));
        }
        
        logger.info("登录成功场景验证通过");
    }
    
    private void verifyLoginFailure(String expectedTip) {
        logger.info("验证登录失败场景，预期提示：{}", expectedTip);
        
        // 验证错误提示元素显示
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertTrue(errorElement.isDisplayed(), "登录失败错误提示未显示");
        
        // 验证错误提示文本
        String actualError = errorElement.getText().trim();
        if (expectedTip != null && !expectedTip.isEmpty()) {
            assertTrue(actualError.contains(expectedTip),
                    String.format("登录失败提示不符合预期：实际=%s，预期包含=%s", actualError, expectedTip));
        }
        
        logger.info("登录失败场景验证通过");
    }
    
    private void logoutSeller() {
        logger.info("执行卖家退出登录操作");
        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
            logoutBtn.click();
            
            // 等待退出后返回登录页或首页
            wait.until(d -> driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().equals(baseUrl));
            logger.info("退出登录完成，当前URL：{}", driver.getCurrentUrl());
        } catch (Exception e) {
            logger.error("退出登录操作失败：{}", e.getMessage(), e);
        }
    }
    
    private String[] detectDelimiter(String line) {
        if (line.contains("\t")) {
            logger.debug("CSV分隔符检测为：制表符（\\t）");
            return line.split("\\t", -1);
        } else {
            logger.debug("CSV分隔符检测为：逗号（,）");
            return line.split(",", -1);
        }
    }
    
    private boolean isValidHeader(String[] headers) {
        Set<String> requiredHeaders = new HashSet<>(Arrays.asList("用例ID", "用户名", "密码", "预期结果"));
        Set<String> actualHeaders = new HashSet<>(Arrays.asList(headers));
        return actualHeaders.containsAll(requiredHeaders);
    }
}