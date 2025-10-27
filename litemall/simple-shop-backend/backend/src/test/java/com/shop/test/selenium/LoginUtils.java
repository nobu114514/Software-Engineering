package com.shop.test.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录工具类，提供各种登录相关的功能
 */
public class LoginUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoginUtils.class);
    
    // 页面元素定位
    private static final By usernameField = By.xpath("//form[@action='/loginIn']//input[1]");
    private static final By passwordField = By.xpath("//form[@action='/loginIn']//input[2]");
    private static final By loginButton = By.xpath("//form[@action='/loginIn']//button[contains(text(), '登录')]");
    private static final By alternativeButton = By.xpath(
        "//input[translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'submit'"
        + " and contains(translate(@value, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '登录')]"
    );
    private static final By logoutButton = By.linkText("退出登录");
    private static final By successMessage = By.xpath("//h2[contains(text(), '登录成功')]");
    
    /**
     * 用户登录方法
     * @param driver WebDriver实例
     * @param username 用户名
     * @param password 密码
     * @return 是否登录成功
     */
    public static boolean login(WebDriver driver, String username, String password) {
        logger.info("开始登录，用户名: {}", username);
        
        try {
            // 导航到登录页面
            driver.get("http://localhost:8080/login");
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // 输入用户名和密码
            wait.until(ExpectedConditions.elementToBeClickable(usernameField)).sendKeys(username);
            wait.until(ExpectedConditions.elementToBeClickable(passwordField)).sendKeys(password);
            
            // 点击登录按钮，尝试多种可能的登录按钮定位器
            try {
                wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
            } catch (Exception e) {
                logger.warn("主登录按钮未找到，尝试备用按钮定位");
                wait.until(ExpectedConditions.elementToBeClickable(alternativeButton)).click();
            }
            
            // 等待页面跳转并验证登录结果
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
                logger.info("登录成功");
                return true;
            } catch (Exception e) {
                logger.error("登录失败: {}", e.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.error("登录过程中出现异常: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 用户退出登录方法
     * @param driver WebDriver实例
     * @return 是否退出成功
     */
    public static boolean logout(WebDriver driver) {
        logger.info("执行退出登录操作");
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
            logoutBtn.click();
            
            // 验证是否返回登录页面
            wait.until(ExpectedConditions.urlContains("login"));
            logger.info("退出登录成功");
            return true;
        } catch (Exception e) {
            logger.error("退出登录失败: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 验证用户是否已登录
     * @param driver WebDriver实例
     * @return 是否已登录
     */
    public static boolean isLoggedIn(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(logoutButton));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}