# A升级包功能自动化测试

## 测试框架结构

本目录包含A升级包功能的自动化测试代码，基于Selenium WebDriver和JUnit 5实现。测试框架结构如下：

```
com/shop/test/selenium/
├── TestBase.java            # 所有测试的基础类
├── LoginUtils.java          # 登录工具类
├── DataDrivenTestBase.java  # 数据驱动测试基类
├── A/                       # A升级包测试目录
│   ├── AUpgradePackageTest.java       # A升级包主测试类
│   ├── RoleManagementTest.java        # 角色管理测试类
│   ├── PermissionManagementTest.java  # 权限管理测试类
│   └── README.md                      # 本说明文档
```

## 测试数据

测试数据存储在CSV文件中，位于：
- `src/test/resources/role_management_test_data.csv` - 角色管理测试数据
- `src/test/resources/permission_management_test_data.csv` - 权限管理测试数据

## 测试环境要求

1. JDK 8或更高版本
2. Maven 3.6+ 或 Gradle 6.0+
3. Chrome浏览器最新版本
4. ChromeDriver（与Chrome浏览器版本匹配）

## 运行测试

### 使用Maven运行

```bash
# 运行所有A升级包测试
mvn test -Dtest=com.shop.test.selenium.A.*

# 运行特定测试类
mvn test -Dtest=com.shop.test.selenium.A.RoleManagementTest

# 运行特定测试方法
mvn test -Dtest=com.shop.test.selenium.A.RoleManagementTest#testAddRole
```

### 使用JUnit IDE运行

在支持JUnit的IDE（如IntelliJ IDEA、Eclipse）中：
1. 右键点击测试类或测试方法
2. 选择"Run"或"Debug"运行测试

## 测试前置条件

1. 系统必须已启动并可访问
2. 管理员账号（默认：admin/admin123）必须存在且有效
3. 数据库中应包含基础数据

## 测试功能覆盖

1. **角色管理测试**
   - 新增角色（成功/失败场景）
   - 删除角色
   - 角色列表验证

2. **权限管理测试**
   - 角色权限分配
   - 权限树层级关系测试
   - 权限保存验证

## 自定义配置

如需修改测试配置，可以在`TestBase.java`中调整以下参数：
- 浏览器类型
- 隐式等待时间
- 页面加载超时
- 系统访问URL

## 注意事项

1. 运行测试前确保系统环境已正确配置
2. 测试过程中不要操作浏览器，以免干扰测试执行
3. 如遇到页面元素定位失败，请检查页面结构是否发生变化
4. 建议使用无头模式运行测试以提高效率

## 问题排查

1. **元素未找到**：检查页面元素定位表达式是否正确
2. **超时错误**：增加等待时间或检查系统响应时间
3. **权限错误**：确保使用正确的管理员账号登录

## 扩展测试

如需扩展测试功能：
1. 在`A`目录下创建新的测试类
2. 继承`DataDrivenTestBase`或`TestBase`
3. 在`resources`目录下添加新的测试数据CSV文件