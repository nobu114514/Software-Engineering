package com.shop.test.selenium.base;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.time.Duration;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonalInfoTest {
    private static final Logger logger = LoggerFactory.getLogger(PersonalInfoTest.class);
    private WebDriver driver;
    private WebDriverWait wait;
    private final String loginUrl = "http://localhost:8080/login";
    private final String csvPath = "src/test/resources/testdata2.csv"; // 对应testdata2.csv路径

    // 页面元素定位器
    // 登录页元素
    private final By usernameField = By.id("name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector("input:nth-child(5)");
    private final By viewInfoLink = By.linkText("查看个人信息");

    // 补充个人信息页元素
    private final By realNameField = By.name("realName");
    private final By phoneField = By.name("phone");
    private final By birthField = By.name("birthDate");
    private final By submitButton = By.xpath("//input[translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='submit']");
    private final By errorTips = By.xpath("//p"); // 假设错误提示统一使用该类

    // 修改密码页元素
    private final By oldPwdField = By.name("oldPassword");
    private final By newPwdField = By.name("newPassword");
    private final By confirmPwdField = By.name("confirmPassword");
    private final By modifyPwdButton = By.xpath("//input[translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='submit']");

    // 修改邮箱页元素
    private final By newEmailField = By.name("newEmail");
    private final By modifyEmailButton = By.xpath("//input[translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='submit']");
    private final By successTips =By.xpath("//p"); 

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
        logger.info("=== 测试清理开始 ===");
        if (driver != null) {
            driver.quit();
        }
        logger.info("=== 测试清理完成 ===");
    }

    // 解析testdata2.csv数据
    public Stream<Map<String, String>> testData() {
        logger.info("=== 加载测试数据开始 ===");
        logger.info("CSV文件路径: {}", csvPath);
        List<Map<String, String>> dataList = new ArrayList<>();

        try {
            String content = new String(Files.readAllBytes(Paths.get(csvPath)), StandardCharsets.UTF_8);
            // 处理BOM头
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }

            try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    logger.error("CSV文件为空");
                    return Stream.empty();
                }
                String[] headers = headerLine.split(",");
                // 清理表头空格
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = headers[i].trim();
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] values = line.split(",");
                    // 清理值空格
                    for (int i = 0; i < values.length; i++) {
                        values[i] = values[i].trim();
                    }

                    Map<String, String> data = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        data.put(headers[i], values[i]);
                    }
                    dataList.add(data);
                    logger.info("添加测试用例: {}", data.get("用例编号"));
                }
            }
            logger.info("=== 加载完成，共{}条测试用例 ===", dataList.size());
        } catch (Exception e) {
            logger.error("读取CSV失败", e);
        }
        return dataList.stream();
    }

    // 登录并进入个人信息页
    private void loginAndNavigateToInfoPage() {
        driver.get(loginUrl);
        logger.info("访问登录页: {}", loginUrl);

        // 输入登录信息（固定账号密码）
        wait.until(ExpectedConditions.elementToBeClickable(usernameField)).sendKeys("testuser1");
        wait.until(ExpectedConditions.elementToBeClickable(passwordField)).sendKeys("Test@1234");
        driver.findElement(loginButton).click();
        logger.info("执行登录操作");

        // 验证登录成功并进入个人信息页
        wait.until(ExpectedConditions.visibilityOfElementLocated(viewInfoLink)).click();
        logger.info("进入个人信息页面");
    }

    // 解析测试数据中的字段值
    private Map<String, String> parseTestData(String dataStr) {
        Map<String, String> dataMap = new HashMap<>();
        if (dataStr == null || dataStr.isEmpty()) return dataMap;

        String[] pairs = dataStr.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split("：");
            if (keyValue.length == 2) {
                dataMap.put(
                    keyValue[0].trim(),
                    keyValue[1].replace("\"", "").trim() // 去除引号
                );
            }
        }
        return dataMap;
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testPersonalInfoFunctions(Map<String, String> testCase) {
        String caseId = testCase.get("用例编号");
        String pageName = testCase.get("页面名称");
        String testFunc = testCase.get("测试功能");
        String expectedResult = testCase.get("预期结果");
        System.out.println(pageName);
        logger.info("=== 执行测试用例: {} [{}] ===", caseId, testFunc);

        try {
            // 前置操作：登录并进入对应页面
            loginAndNavigateToInfoPage();

            // 根据页面名称跳转至目标页面
            switch (pageName) {
            case "修改补充信息":
                try {
                    By linkLocator = By.linkText("修改补充信息");
                    WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));
                    logger.info("超链接文本: " + link.getText());
                    logger.info("超链接URL: " + link.getAttribute("href"));
                    link.click();
                    wait.until(ExpectedConditions.urlContains("additionalInfo"));
                    testPersonalInfoPage(testCase);
                    
                } catch (NoSuchElementException e) {
                    logger.error("未找到'修改补充信息'超链接", e);
                    fail("超链接定位失败");
                } catch (TimeoutException e) {
                    logger.error("等待超链接加载超时", e);
                    fail("超链接加载超时");
                }
                break;
                case "修改密码":
                    driver.findElement(By.linkText("修改密码")).click();
                    testPasswordModifyPage(testCase);
                    break;
                case "修改邮箱":
                    driver.findElement(By.linkText("修改邮箱")).click();
                    testEmailModifyPage(testCase);
                    break;
                default:
                    logger.warn("未知页面: {}", pageName);
            }
        } catch (Exception e) {
            logger.error("测试用例{}执行失败", caseId, e);
            fail("测试用例" + caseId + "执行异常: " + e.getMessage());
        }
        logger.info("=== 测试用例: {} 执行完成 ===", caseId);
    }

    // 补充个人信息页面测试
    private void testPersonalInfoPage(Map<String, String> testCase) {
        Map<String, String> dataMap = parseTestData(testCase.get("测试数据"));

        // 输入表单数据
        wait.until(ExpectedConditions.elementToBeClickable(realNameField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(realNameField)).sendKeys(dataMap.getOrDefault("真实姓名", ""));

        wait.until(ExpectedConditions.elementToBeClickable(phoneField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(phoneField)).sendKeys(dataMap.getOrDefault("电话号码", ""));

        wait.until(ExpectedConditions.elementToBeClickable(birthField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(birthField)).sendKeys(dataMap.getOrDefault("出生日期", ""));

        // 提交表单
        driver.findElement(submitButton).click();
        
        // 验证结果
        String expected = testCase.get("预期结果");
        if (expected.contains("补充信息成功")) {
        	String actualText = wait.until(ExpectedConditions.visibilityOfElementLocated(successTips))
                    .getText();
        	assertEquals("补充信息成功", actualText);
        } else {
        	String actualText2 = wait.until(ExpectedConditions.visibilityOfElementLocated(errorTips))
                    .getText();
            assertTrue(!actualText2.isEmpty(), "未显示错误提示");
         // 直接检查错误提示是否包含预期的完整信息
            assertTrue(expected.contains(actualText2));
        }
    }

    // 修改密码页面测试
    private void testPasswordModifyPage(Map<String, String> testCase) {
        Map<String, String> dataMap = parseTestData(testCase.get("测试数据"));

        // 输入密码数据
        wait.until(ExpectedConditions.elementToBeClickable(oldPwdField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(oldPwdField)).sendKeys(dataMap.getOrDefault("旧密码", ""));

        wait.until(ExpectedConditions.elementToBeClickable(newPwdField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(newPwdField)).sendKeys(dataMap.getOrDefault("新密码", ""));

        wait.until(ExpectedConditions.elementToBeClickable(confirmPwdField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(confirmPwdField)).sendKeys(dataMap.getOrDefault("确认新密码", ""));

        // 提交修改
        driver.findElement(modifyPwdButton).click();

        // 验证结果
        String expected = testCase.get("预期结果");
        if (expected.contains("修改密码成功")) {
        	String actualText = wait.until(ExpectedConditions.visibilityOfElementLocated(successTips))
                    .getText();
        	assertEquals("修改密码成功", actualText);
        } else {
        	String actualText2 = wait.until(ExpectedConditions.visibilityOfElementLocated(errorTips))
                    .getText();
            assertTrue(!actualText2.isEmpty(), "未显示错误提示");
         // 直接检查错误提示是否包含预期的完整信息
            assertTrue(expected.contains(actualText2));
        }
    }

    // 修改邮箱页面测试
    private void testEmailModifyPage(Map<String, String> testCase) {
        String email = testCase.get("测试数据").replace("新邮箱：\"", "").replace("\"", "").trim();

        // 输入邮箱
        wait.until(ExpectedConditions.elementToBeClickable(newEmailField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(newEmailField)).sendKeys(email);

        // 提交修改
        driver.findElement(modifyEmailButton).click();

        // 验证结果
        String expected = testCase.get("预期结果");
        if (expected.contains("邮箱修改成功")) {
        	String actualText = wait.until(ExpectedConditions.visibilityOfElementLocated(successTips))
                    .getText();
        	assertEquals("邮箱修改成功", actualText);
        } else {
        	String actualText2 = wait.until(ExpectedConditions.visibilityOfElementLocated(errorTips))
                    .getText();
            assertTrue(!actualText2.isEmpty(), "未显示错误提示");
         // 直接检查错误提示是否包含预期的完整信息
            assertTrue(expected.contains(actualText2));
        }
    }
}