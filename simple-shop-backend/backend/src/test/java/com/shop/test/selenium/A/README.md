# 升级包A自动化测试框架

## 测试框架结构

### 1. 基础测试类
- `UpgradeATestBase.java` - 升级包A测试的基础类，提供通用功能
  - 浏览器初始化和清理
  - 管理员/用户登录方法
  - 等待页面加载
  - 测试数据加载
  - 元素查找安全方法
  - 截图功能

### 2. 商品管理测试
- `ProductManagementTest.java` - 商品管理模块测试类
  - 测试用例覆盖：TC_PROD_001 到 TC_PROD_007
  - 包含商品列表显示、添加、查询、编辑、删除、库存管理和分类管理测试

### 3. 客户管理测试
- `CustomerManagementTest.java` - 客户管理模块测试类
  - 测试用例覆盖：TC_CUST_001 到 TC_CUST_007
  - 包含客户列表显示、查询、详情查看、信息编辑、等级管理、统计功能和分组管理测试

## 测试数据文件

### 1. 商品管理测试数据
- `src/test/resources/product_management_test_data.csv`
- 包含所有商品管理测试用例的数据

### 2. 客户管理测试数据
- `src/test/resources/customer_management_test_data.csv`
- 包含所有客户管理测试用例的数据

## 环境要求

1. JDK 1.8+
2. Maven 3.6+
3. Chrome浏览器
4. 对应的ChromeDriver版本
5. Selenium WebDriver 4.x

## 运行方法

### 方法1：使用JUnit直接运行

在IDE中右键点击测试类，选择"Run as JUnit Test"。

### 方法2：使用Maven命令运行

```bash
# 运行所有测试
mvn test

# 运行特定的测试类
mvn test -Dtest=ProductManagementTest
mvn test -Dtest=CustomerManagementTest

# 运行特定的测试方法
mvn test -Dtest=ProductManagementTest#testAddProduct
```

## 前置条件

1. 确保测试环境已启动，应用可正常访问
2. 确保默认管理员账号密码正确（admin/123456）
3. 测试数据文件已正确放置在resources目录下

## 功能覆盖

### 商品管理模块
- TC_PROD_001: 商品列表显示测试
- TC_PROD_002: 添加新商品测试
- TC_PROD_003: 商品查询测试
- TC_PROD_004: 编辑商品信息测试
- TC_PROD_005: 删除商品测试
- TC_PROD_006: 商品库存管理测试
- TC_PROD_007: 商品分类管理测试

### 客户管理模块
- TC_CUST_001: 客户列表显示测试
- TC_CUST_002: 客户查询测试
- TC_CUST_003: 客户详情查看测试
- TC_CUST_004: 客户信息编辑测试
- TC_CUST_005: 客户等级管理测试
- TC_CUST_006: 客户统计功能测试
- TC_CUST_007: 客户分组管理测试

## 自定义配置

### 修改基础URL
在 `UpgradeATestBase.java` 中修改 `baseUrl` 变量

### 修改默认登录凭证
在测试类的 `beforeTest()` 方法中修改 `adminLogin()` 调用的参数

## 注意事项

1. 测试执行过程中请勿操作浏览器
2. 确保浏览器驱动版本与Chrome浏览器版本匹配
3. 如遇元素定位失败，可能需要根据实际页面结构调整定位器
4. 测试数据请根据实际环境进行调整

## 问题排查

1. **浏览器无法启动**：检查ChromeDriver是否正确安装，版本是否匹配
2. **登录失败**：验证管理员账号密码是否正确
3. **元素找不到**：检查页面结构是否变化，调整定位器
4. **测试超时**：可适当增加等待时间

## 测试报告

运行测试后，Maven Surefire会生成测试报告在 `target/surefire-reports` 目录下。