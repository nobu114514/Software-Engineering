package com.shop.test.selenium.base;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.NoSuchElementException;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;
import org.openqa.selenium.support.ui.WebDriverWait; // 新增导入
import org.openqa.selenium.support.ui.ExpectedConditions; // 新增导入
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginUITest {
    private WebDriver driver;
    private final String loginUrl = "http://localhost:8080/login";
    private final String csvPath = "src/test/resources/testdata.csv";
    private WebDriverWait wait;
    // 页面元素定位
    private final By usernameField = By.xpath("//form[@action='/loginIn']//input[1]");
    private final By passwordField = By.xpath("//form[@action='/loginIn']//input[2]");
    private final By loginButton = By.xpath("//form[@action='/loginIn']//button[contains(text(), '登录')]");
    private final By registerLink = By.linkText("注册");
    private final By logoutButton = By.linkText("退出登录");
    private final By successMessage = By.xpath("//h2[contains(text(), '登录成功')]"); 
    private final By errorMessage = By.xpath("//p");
    @BeforeAll
    public void setup() {
        System.out.println("=== 测试初始化开始 ===");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                System.out.println("读取到的表头: " + headerLine);
                if (headerLine == null || 
                    (!headerLine.contains("用例 ID") && 
                     !headerLine.contains("user") && 
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
                        System.out.println("读取到的密码: " + data.get("密码"));
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
 // 等待页面加载完成的方法
    private void waitForPageLoad() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }
 // 检查会话是否有效的方法
    private boolean isSessionValid() {
        try {
            driver.getTitle();
            return true;
        } catch (NoSuchSessionException e) {
            return false;
        }
    }

    // 重新初始化WebDriver的方法
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
    private String[] detectDelimiter(String line) {
        if (line.contains("\t")) {
            return line.split("\\t", -1);
        } else {
            return line.split(",", -1);
        }
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testLoginFunctionality(Map<String, String> testCase) {
        System.out.println("=== 执行测试用例: " + testCase.get("用例 ID") + " ===");

        // 访问登录页面
        driver.get(loginUrl);
        System.out.println("当前页面URL: " + driver.getCurrentUrl());

        // 等待页面完全加载
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

        // 提取测试数据
        String username = testCase.get("user");
        String password = testCase.get("password");
        System.out.println("从CSV获取到的用户名: " + username);
        System.out.println("从CSV获取到的密码: " + password);
        String expectedResult = testCase.get("预期结果");

        // 输入账号密码
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(usernameField))
                .sendKeys(username != null ? username : "");

            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(passwordField))
                .sendKeys(password != null ? password : "");
                
            System.out.println("账号密码输入成功");
        } catch (Exception e) {
            System.err.println("输入账号密码时出现异常: " + e.getMessage());
            e.printStackTrace();
        }

        // 使用单独的方法处理登录按钮点击
        try {
            submitForm();
        } catch (Exception e) {
            System.err.println("提交表单及处理页面跳转时出现异常: " + e.getMessage());
            e.printStackTrace();
        }
        // 验证结果部分

        if (expectedResult != null && expectedResult.contains("登录成功")) {
            // 成功场景验证
            try {
                // 等待并验证成功消息
            	System.out.println("登录成功");
                WebElement successMsg = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successMessage)
                );
                assertTrue(successMsg.isDisplayed(), "登录成功消息未显示");
                System.out.println("成功消息验证通过");

                // 验证登录后URL
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("loginIn"), "登录后未跳转到预期页面");

                // 点击退出按钮
                WebElement logoutBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(logoutButton)
                );
                logoutBtn.click();
                System.out.println("已点击退出登录");

                // 验证返回登录页面
                wait.until(ExpectedConditions.urlContains("login"));
                assertEquals("http://localhost:8080/login", driver.getCurrentUrl(), "退出后未返回登录页面");
                System.out.println("退出验证通过");

            } catch (Exception e) {
                System.err.println("成功场景验证时出现异常: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 失败场景验证
            try {
                // 等待并获取<p>标签元素
                WebElement errorMsg = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(errorMessage)
                );
                String actualError = errorMsg.getText().trim();
                // 假设预期结果中会包含可能的错误提示关键词
                String expectedErr = expectedResult;
                // 进行宽泛的包含判断
                assertTrue(actualError.toLowerCase().contains(expectedErr.toLowerCase()), 
                        "错误信息不匹配 - 预期: " + expectedErr + ", 实际: " + actualError);
                System.out.println("错误消息验证通过");

                // 验证登录失败后URL
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("login"), "登录失败后未停留在登录页面");

            } catch (Exception e) {
                System.err.println("失败场景验证时出现异常: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("=== 测试用例执行完成 ===");
    }
    private void submitForm() {

            try {
                By alternativeLocator = By.xpath(
                    "//input[translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'submit'" +
                    " and contains(translate(@value, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '登录')]"
                );
                WebElement alternativeButton = wait.until(
                    ExpectedConditions.elementToBeClickable(alternativeLocator)
                );
                alternativeButton.click();
            } catch (Exception ex) {
            	System.err.println("失败场景验证时出现异常: " + ex.getMessage());
                ex.printStackTrace();
            }
    }
}