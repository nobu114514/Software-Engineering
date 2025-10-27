package com.shop.test.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试基础类，提供所有测试共享的功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestBase {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final String baseUrl = "http://localhost:8080";
    
    @BeforeAll
    public void setup() {
        logger.info("=== 测试初始化开始 ===");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logger.info("=== 浏览器初始化完成 ===");
    }

    @AfterAll
    public void tearDown() {
        logger.info("=== 测试清理开始 ===");
        if (driver != null) {
            driver.quit();
        }
        logger.info("=== 测试清理完成 ===");
    }
    
    /**
     * 等待页面加载完成
     */
    protected void waitForPageLoad() {
        wait.until(d -> ((org.openqa.selenium.JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }
    
    /**
     * 导航到指定URL
     */
    protected void navigateTo(String url) {
        driver.get(baseUrl + url);
        waitForPageLoad();
        logger.info("导航到: {}", driver.getCurrentUrl());
    }
}