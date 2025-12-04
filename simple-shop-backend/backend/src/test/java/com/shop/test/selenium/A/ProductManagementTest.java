package com.shop.test.selenium.A;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductManagementTest {
    private static final Logger logger = LoggerFactory.getLogger(ProductManagementTest.class);
    
    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "http://localhost:8080/";
    
    // 页面元素定位器
    private final By sellerEntryLink = By.xpath("//a[contains(@href, 'seller') or contains(text(), '卖家')]");
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector(".btn");
    private final By publishNewProductLink = By.xpath("//a[contains(@href, 'publish') or contains(text(), '发布')]");
    private final By batchPublishProductLink = By.xpath("//button[contains(text(), '批量') or contains(text(), '批量发布')]");
    private final By addRowButton = By.cssSelector(".btn-circle.add-btn");
    private final By removeRowButton = By.cssSelector(".btn-circle.remove-btn");
    private final By batchTable = By.cssSelector(".batch-table");
    private final By addButtonContainer = By.cssSelector(".add-button-container");
    private final By productRows = By.cssSelector(".batch-table tbody tr");
    private final By batchPublishButton = By.cssSelector(".form-actions .btn-primary");
    private final By batchReturnButton = By.cssSelector(".form-actions .btn-secondary");
    
    // 根据表单HTML更新的元素定位器
    private final By productNameField = By.id("name");           // 商品名称
    private final By productCategorySelect = By.id("category");   // 商品分类
    private final By productSubCategorySelect = By.id("subCategory"); // 商品子分类
    private final By productPriceField = By.id("price");         // 商品价格
    private final By productImageUrlField = By.id("imageUrl");   // 商品主图URL
    private final By productStockField = By.id("stock");         // 商品库存
    private final By productCodeField = By.xpath("//input[contains(@id, 'code') or contains(@name, 'code')]"); // 商品编码（表单中未明确，但测试需要）
    
    // 兼容旧的分类选择器命名
    private final By firstCategorySelect = productCategorySelect;
    private final By secondCategorySelect = productSubCategorySelect;
    
    private final By submitProductButton = By.cssSelector("form button[type='submit']"); // 发布商品按钮
    private final By productHistoryLink = By.xpath("//a[contains(text(), '商品历史') or contains(@href, 'product')]");
    private final By sellerDashboardLink = By.xpath("//a[contains(text(), '卖家后台') or contains(@href, 'dashboard')]");
    private final By logoutButton = By.cssSelector(".actions > .btn:nth-child(1)");
    private final By confirmLogoutButton = By.cssSelector("button:nth-child(2)");
    private final By successMessage = By.xpath("//*[contains(@class, 'success') or contains(text(), '成功')]");
    private final By errorMessage = By.xpath("//*[contains(@class, 'error') or contains(@class, 'alert') or contains(@class, 'warning')]");
    private final By helpText = By.cssSelector(".help-text"); // 辅助文本
    
    @BeforeAll
    public void setup() {
        logger.info("=== 商品管理测试 - 初始化开始 ===");
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20), Duration.ofMillis(500));
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            logger.info("=== 商品管理测试 - 浏览器初始化完成 ===");
        } catch (Exception e) {
            logger.error("=== 商品管理测试 - 初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("浏览器初始化失败，测试无法继续", e);
        }
    }
    
    @AfterAll
    public void tearDown() {
        logger.info("=== 商品管理测试 - 清理开始 ===");
        try {
            if (driver != null) {
                // 确保先退出登录
                try {
                    logout();
                } catch (Exception e) {
                    logger.warn("退出登录失败，但继续关闭浏览器: {}", e.getMessage());
                }
                driver.quit();
                logger.info("浏览器已关闭");
            }
        } catch (NoSuchSessionException e) {
            logger.warn("浏览器会话已不存在，无需重复关闭: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("关闭浏览器时发生异常: {}", e.getMessage(), e);
        } finally {
            logger.info("=== 商品管理测试 - 清理完成 ===");
        }
    }
    
    // 等待页面加载完成
    private void waitForPageLoad() {
        try {
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            logger.info("页面加载完成");
        } catch (Exception e) {
            logger.warn("页面加载等待超时，但继续执行: {}", e.getMessage());
        }
    }
    
    // 登录方法
    private void login(String username, String password) {
        try {
            logger.info("执行登录操作，用户名: {}", username);
            driver.get(baseUrl);
            waitForPageLoad();
            
            // 点击卖家入口
            WebElement sellerEntry = wait.until(ExpectedConditions.elementToBeClickable(sellerEntryLink));
            sellerEntry.click();
            waitForPageLoad();
            
            // 输入用户名和密码
            WebElement userElement = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
            userElement.clear();
            userElement.sendKeys(username);
            
            WebElement passElement = driver.findElement(passwordField);
            passElement.clear();
            passElement.sendKeys(password);
            
            // 点击登录按钮
            WebElement loginBtn = driver.findElement(loginButton);
            loginBtn.click();
            waitForPageLoad();
            
            // 验证登录成功（等待发布商品链接出现）
            wait.until(ExpectedConditions.elementToBeClickable(publishNewProductLink));
            logger.info("登录成功，已进入卖家后台");
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage(), e);
            captureScreenshot("login_failure");
            throw new RuntimeException("登录失败，测试无法继续", e);
        }
    }
    
    // 退出登录方法
    private void logout() {
        try {
            logger.info("执行退出登录操作");
            
            // 尝试多种定位策略查找退出登录按钮
            WebElement logoutBtn = null;
            
            // 添加精确文本匹配的定位器作为第一个尝试的选项
            By exactTextLocator = By.xpath("//button[text()='退出登录']");
            
            // 尝试使用精确文本匹配定位
            try {
                List<WebElement> elements = driver.findElements(exactTextLocator);
                if (!elements.isEmpty()) {
                    logoutBtn = elements.get(0);
                    if (logoutBtn.isDisplayed() && logoutBtn.isEnabled()) {
                        logger.info("使用精确文本匹配找到退出登录按钮");
                    } else {
                        logoutBtn = null;
                    }
                }
            } catch (Exception e) {
                logger.debug("尝试精确文本匹配定位器失败: {}", e.getMessage());
            }
            
            // 如果精确匹配未找到，使用原有的定位策略列表
            if (logoutBtn == null) {
                List<By> logoutLocators = Arrays.asList(
                    // 原有的定位器
                    By.cssSelector(".actions > .btn:nth-child(1)"),
                    // 基于文本的定位器
                    By.xpath("//button[contains(text(), '退出') or contains(text(), 'Logout')]"),
                    // 基于类名和ID的定位器
                    By.className("logout"),
                    By.id("logout"),
                    // 通用按钮定位器
                    By.cssSelector(".btn.logout, .logout-btn, [ng-click*='logout']"),
                    // 顶部导航栏中的退出链接
                    By.xpath("//a[contains(@href, 'logout') or contains(text(), '退出')]"),
                    // 卖家后台页面常见退出按钮位置
                    By.xpath("//div[contains(@class, 'header') or contains(@class, 'nav')]//button[contains(text(), '退出')]")
                );
                
                // 尝试每种定位器直到找到元素
                for (By locator : logoutLocators) {
                    try {
                        List<WebElement> elements = driver.findElements(locator);
                        if (!elements.isEmpty()) {
                            logoutBtn = elements.get(0);
                            if (logoutBtn.isDisplayed() && logoutBtn.isEnabled()) {
                                logger.info("使用定位器 {} 找到退出按钮", locator);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("尝试定位器 {} 失败: {}", locator, e.getMessage());
                    }
                }
            }
            
            if (logoutBtn != null) {
                // 使用Javascript点击作为备选方案
                try {
                    logger.info("尝试点击退出按钮");
                    logoutBtn.click();
                } catch (ElementNotInteractableException e) {
                    logger.warn("常规点击失败，尝试使用JavaScript点击");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
                }
                
                waitForPageLoad();
                
                // 尝试多种方式查找确认退出按钮
                WebElement confirmBtn = null;
                List<By> confirmLocators = Arrays.asList(
                    By.cssSelector("button:nth-child(2)"),
                    By.xpath("//button[contains(text(), '确定') or contains(text(), '确认') or contains(text(), 'Yes')]"),
                    By.className("confirm"),
                    By.id("confirm-logout")
                );
                
                // 尝试查找确认按钮
                for (By locator : confirmLocators) {
                    try {
                        List<WebElement> elements = driver.findElements(locator);
                        if (!elements.isEmpty()) {
                            confirmBtn = elements.get(0);
                            if (confirmBtn.isDisplayed() && confirmBtn.isEnabled()) {
                                logger.info("使用定位器 {} 找到确认按钮", locator);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("尝试确认按钮定位器 {} 失败: {}", locator, e.getMessage());
                    }
                }
                
                // 如果找到确认按钮就点击
                if (confirmBtn != null) {
                    try {
                        confirmBtn.click();
                    } catch (ElementNotInteractableException e) {
                        logger.warn("常规点击确认按钮失败，尝试使用JavaScript点击");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
                    }
                    waitForPageLoad();
                } else {
                    logger.info("未找到确认退出按钮，可能不需要确认");
                }
                
                logger.info("退出登录成功");
            } else {
                // 如果找不到退出按钮，尝试直接访问退出URL
                try {
                    logger.warn("未找到退出按钮，尝试直接访问退出URL");
                    driver.get(baseUrl + "/logout");
                    waitForPageLoad();
                    driver.get(baseUrl + "/seller/logout");
                    waitForPageLoad();
                    logger.info("通过URL退出登录成功");
                } catch (Exception e) {
                    logger.error("通过URL退出登录也失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("退出登录失败: {}", e.getMessage());
            // 尝试刷新页面后再退出
            try {
                driver.navigate().refresh();
                Thread.sleep(1000);
                logout(); // 递归调用再次尝试
            } catch (Exception innerE) {
                logger.error("刷新页面后再次退出也失败: {}", innerE.getMessage());
            }
        }
    }
    
    // 截图方法
    private void captureScreenshot(String prefix) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File srcFile = screenshot.getScreenshotAs(OutputType.FILE);
            String fileName = "screenshot_" + prefix + "_" + System.currentTimeMillis() + ".png";
            File destFile = new File(fileName);
            Files.copy(srcFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("已保存截图: {}", destFile.getAbsolutePath());
        } catch (Exception e) {
            logger.warn("截图失败: {}", e.getMessage());
        }
    }
    
    // 辅助方法：选择下拉框选项
    private void selectDropdownOption(WebElement dropdown, String optionText) {
        try {
            Select select = new Select(dropdown);
            select.selectByVisibleText(optionText);
            logger.info("已选择下拉框选项: {}", optionText);
        } catch (Exception e) {
            logger.error("选择下拉框选项失败: {}", e.getMessage());
            // 尝试直接点击选项
            try {
                WebElement option = dropdown.findElement(By.xpath(".//option[contains(text(), '" + optionText + "')]"));
                option.click();
                logger.info("通过点击选择了选项: {}", optionText);
            } catch (Exception innerE) {
                logger.error("点击选择选项也失败: {}", innerE.getMessage());
                throw new RuntimeException("无法选择下拉框选项: " + optionText, e);
            }
        }
    }
    
    // 辅助方法：根据值选择下拉框选项
    private void selectOptionByValue(WebElement dropdown, String optionValue) {
        try {
            Select select = new Select(dropdown);
            select.selectByValue(optionValue);
            logger.info("已根据值选择下拉框选项: {}", optionValue);
        } catch (Exception e) {
            logger.error("根据值选择下拉框选项失败: {}", e.getMessage());
            // 尝试直接点击选项
            try {
                WebElement option = dropdown.findElement(By.xpath(".//option[@value='" + optionValue + "']"));
                option.click();
                logger.info("通过点击选择了值为 {} 的选项", optionValue);
            } catch (Exception innerE) {
                logger.error("点击选择选项也失败: {}", innerE.getMessage());
                throw new RuntimeException("无法选择下拉框选项值: " + optionValue, e);
            }
        }
    }
    
    /**
     * 测试批量发布商品 - 用例18871
     * 前置条件：卖家已登录
     * 步骤：1.进入批量发布商品界面 2.添加商品：苹果，数码产品，手机，4599，1，PHONE001；始祖鸟，服装鞋帽，男士服装，2999，1，T-S001
     * 预期：商品一二级添加成功，批量发布成功
     */
    @Test
    public void testBatchProductPublish() {
        logger.info("=== 开始执行批量发布商品测试（用例18871）===");
        
        try {
            // 登录卖家账号
            login("seller", "password");
            
            // 点击发布新商品
            wait.until(ExpectedConditions.elementToBeClickable(publishNewProductLink)).click();
            waitForPageLoad();
            
            // 点击批量发布商品按钮
            WebElement batchPublishBtn = wait.until(ExpectedConditions.elementToBeClickable(batchPublishProductLink));
            batchPublishBtn.click();
            waitForPageLoad();
            
            // 确保页面加载完成（表格形式的批量发布界面）
            wait.until(ExpectedConditions.visibilityOfElementLocated(batchTable));
            logger.info("批量发布页面加载完成，表格可见");
            
            // 批量添加商品（表格形式）
            // 商品1: 苹果（第一行，索引为0）
            fillProductRow(0, "苹果", "数码产品", "手机", "4599", "1", "PHONE001");
            
            // 检查是否需要添加第二行
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productRows));
            if (rows.size() < 2) {
                // 点击添加按钮
                WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addRowButton));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addBtn);
                Thread.sleep(300);
                addBtn.click();
                logger.info("添加第二行");
                
                // 等待第二行加载
                wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(productRows, 1));
            }
            
            // 商品2: 始祖鸟（第二行，索引为1）
            fillProductRow(1, "始祖鸟", "服装鞋帽", "男士服装", "2999", "1", "T-S001");
            
            // 点击批量发布按钮
            WebElement publishButton = wait.until(ExpectedConditions.elementToBeClickable(batchPublishButton));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", publishButton);
            Thread.sleep(300);
            publishButton.click();
            logger.info("点击批量发布按钮");
            
            // 等待成功提示 (使用多种定位策略)
            boolean success = false;
            try {
                // 尝试通过CSS选择器
                WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        successMessage));
                success = successMsg.isDisplayed();
            } catch (Exception e) {
                logger.warn("未找到成功消息元素，检查页面标题变化");
                // 检查URL或标题变化
                try {
                    wait.until(ExpectedConditions.urlContains("product/list"));
                    success = true;
                } catch (Exception ex) {
                    logger.warn("URL未切换到列表页面");
                }
            }
            
            assertTrue(success, "批量发布成功验证失败");
            logger.info("批量发布商品测试成功");
            
        } catch (Exception e) {
            logger.error("批量商品发布测试失败: {}", e.getMessage(), e);
            captureScreenshot("batch_product_publish_failure");
            fail("批量商品发布测试失败: " + e.getMessage());
        } finally {
            // 确保退出登录
            logout();
        } 
    }
    
    /**
     * 测试批量发布减少行 - 用例18802
     * 前置条件：管理员已登录
     * 步骤：1.点击发布商品按钮 2.点击批量发布商品按钮 3.点击-删除一行商品
     * 预期：呈现多商品录入列表，行删除成功
     */
    @Test
    public void testBatchProductRemoveRow() {
        logger.info("=== 开始执行批量发布减少行测试（用例18802）===");
        
        try {
            // 登录卖家账号
            login("seller", "password");
            
            // 点击发布新商品
            wait.until(ExpectedConditions.elementToBeClickable(publishNewProductLink)).click();
            waitForPageLoad();
            
            // 点击批量发布商品按钮
            WebElement batchPublishBtn = wait.until(ExpectedConditions.elementToBeClickable(batchPublishProductLink));
            batchPublishBtn.click();
            waitForPageLoad();
            
            // 确保页面加载完成（表格形式的批量发布界面）
            wait.until(ExpectedConditions.visibilityOfElementLocated(batchTable));
            logger.info("批量发布页面加载完成，表格可见");
            
            // 检查初始行数
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productRows));
            int initialRowCount = rows.size();
            logger.info("初始行数: {}", initialRowCount);
            
            // 如果初始行数小于2，添加一行
            if (initialRowCount < 2) {
                WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addRowButton));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addBtn);
                Thread.sleep(300);
                addBtn.click();
                logger.info("添加一行以确保有足够的行进行删除测试");
                
                // 等待新行加载
                wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(productRows, initialRowCount));
                rows = driver.findElements(productRows);
                initialRowCount = rows.size();
            }
            
            // 填写第二行信息，以便识别
            try {
                // 使用表格方式填写第二行
                WebElement secondRow = rows.get(1);
                WebElement nameField = secondRow.findElements(By.tagName("td")).get(1).findElement(By.tagName("input"));
                nameField.clear();
                nameField.sendKeys("待删除测试商品");
            } catch (Exception e) {
                logger.warn("填写第二行信息失败，但继续测试删除功能: {}", e.getMessage());
            }
            
            // 删除第二行（从0开始索引，所以删除索引为1的行）
            WebElement rowToDelete = rows.get(1);
            WebElement removeBtn = rowToDelete.findElements(By.tagName("td")).get(8).findElement(removeRowButton);
            
            // 滚动到删除按钮可见
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", removeBtn);
            Thread.sleep(300);
            
            // 点击删除按钮
            try {
                removeBtn.click();
            } catch (ElementNotInteractableException e) {
                // 如果直接点击失败，使用JavaScript点击
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", removeBtn);
                logger.info("使用JavaScript点击删除按钮");
            }
            
            // 等待行数变化
            try {
                wait.until(ExpectedConditions.numberOfElementsToBe(productRows, initialRowCount - 1));
            } catch (Exception e) {
                // 如果等待超时，我们仍然继续验证
                logger.warn("等待行数变化超时，继续验证");
            }
            
            // 再次获取行数并验证
            int finalRowCount = driver.findElements(productRows).size();
            logger.info("删除后行数: {}", finalRowCount);
            assertTrue(finalRowCount < initialRowCount, "行数应该减少");
            
            logger.info("批量发布减少行测试成功");
            
        } catch (Exception e) {
            logger.error("批量发布减少行测试失败: {}", e.getMessage(), e);
            captureScreenshot("batch_remove_row_failure");
            fail("批量发布减少行测试失败: " + e.getMessage());
        } finally {
            // 确保退出登录
            logout();
        } 
    }
    
    // 辅助方法：填写商品行信息（用于批量发布）
    private void fillProductRow(int rowIndex, String productName, String category, String subCategory, 
                              String price, String stock, String code) throws Exception {
        try {
            // 获取对应行（表格形式的批量发布，从0开始索引）
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productRows));
            if (rowIndex >= rows.size()) {
                logger.error("行索引超出范围: 要求行 {}, 实际行数 {}", rowIndex + 1, rows.size());
                throw new IndexOutOfBoundsException("商品行索引超出范围");
            }
            
            WebElement row = rows.get(rowIndex);
            
            // 商品名称 (第2个th后面的td)
            WebElement nameField = row.findElements(By.tagName("td")).get(1).findElement(By.tagName("input"));
            nameField.clear();
            nameField.sendKeys(productName);
            
            // 商品分类 (第3个th后面的td)
            WebElement categoryField = row.findElements(By.tagName("td")).get(2).findElement(By.tagName("select"));
            // 对于第二行及以后，可以选择"同上"选项
            if (rowIndex > 0) {
                try {
                    // 尝试选择"同上"选项
                    selectOptionByValue(categoryField, "same");
                    logger.info("第{}行分类选择了'同上'", rowIndex + 1);
                } catch (Exception e) {
                    // 如果没有"同上"选项或选择失败，尝试通过文本选择
                    try {
                        selectDropdownOption(categoryField, category);
                    } catch (Exception ex) {
                        // 最后尝试通过值选择
                        selectOptionByValue(categoryField, category);
                    }
                }
            } else {
                // 第一行直接选择分类
                try {
                    selectDropdownOption(categoryField, category);
                } catch (Exception ex) {
                    selectOptionByValue(categoryField, category);
                }
            }
            
            // 等待子分类更新
            Thread.sleep(500);
            
            // 商品子分类 (第4个th后面的td)
            WebElement subCategoryField = row.findElements(By.tagName("td")).get(3).findElement(By.tagName("select"));
            // 对于第二行及以后，可以选择"同上"选项
            if (rowIndex > 0) {
                try {
                    selectOptionByValue(subCategoryField, "same");
                    logger.info("第{}行子分类选择了'同上'", rowIndex + 1);
                } catch (Exception e) {
                    // 子分类的值是[object Object]，但文本应该是正常的
                    selectDropdownOption(subCategoryField, subCategory);
                }
            } else {
                // 第一行直接选择子分类
                selectDropdownOption(subCategoryField, subCategory);
            }
            
            // 商品价格 (第5个th后面的td)
            WebElement priceField = row.findElements(By.tagName("td")).get(4).findElement(By.tagName("input"));
            priceField.clear();
            priceField.sendKeys(price);
            
            // 商品主题 (第6个th后面的td) - 非必填，但我们也填一下
            try {
                WebElement themeField = row.findElements(By.tagName("td")).get(5).findElement(By.tagName("input"));
                themeField.clear();
                themeField.sendKeys("测试主题");
            } catch (Exception e) {
                logger.warn("未找到商品主题字段，跳过");
            }
            
            // 商品详情 (第7个th后面的td)
            try {
                WebElement detailField = row.findElements(By.tagName("td")).get(6).findElement(By.tagName("textarea"));
                detailField.clear();
                detailField.sendKeys("这是测试商品详情，用于自动化测试。");
            } catch (Exception e) {
                logger.warn("未找到商品详情字段，跳过");
            }
            
            // 库存 (第8个th后面的td)
            WebElement stockField = row.findElements(By.tagName("td")).get(7).findElement(By.tagName("input"));
            stockField.clear();
            stockField.sendKeys(stock);
            
            // 尝试填写编码（如果表单支持）
            try {
                // 尝试在该行中查找编码字段
                List<WebElement> inputFields = row.findElements(By.tagName("input"));
                if (inputFields.size() > 4) { // 假设至少有名称、价格、库存、主题字段
                    WebElement codeField = inputFields.get(inputFields.size() - 1); // 尝试最后一个input
                    codeField.clear();
                    codeField.sendKeys(code);
                }
            } catch (Exception e) {
                logger.warn("未找到商品编码字段，跳过: {}", e.getMessage());
            }
            
            logger.info("成功填写第{}行商品信息: {}", rowIndex + 1, productName);
            
        } catch (Exception e) {
            logger.error("填写第{}行商品信息时出错: {}", rowIndex + 1, e.getMessage());
            captureScreenshot("fill_product_row_error_");
            throw e;
        }
    }
}