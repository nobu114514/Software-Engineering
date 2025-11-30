@echo off

rem 在线购物系统 Selenium 测试运行脚本
rem 此脚本用于运行所有 Selenium 自动化测试

setlocal enabledelayedexpansion

echo =======================================
echo 在线购物系统 Selenium 测试运行脚本
set "scriptPath=%~dp0"
echo 当前目录: !scriptPath!
echo =======================================

rem 检查 Java 环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 Java 环境，请确保 JDK 11 已正确安装并配置环境变量
    pause
    exit /b 1
)

echo 检测到 Java 环境，继续执行...

rem 检查 Maven 环境
mvn -v >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 Maven 环境，请确保 Maven 3.6+ 已正确安装并配置环境变量
    pause
    exit /b 1
)

echo 检测到 Maven 环境，继续执行...

rem 检查服务状态（简单的网络请求检查）
echo 正在检查后端服务状态...
ping -n 1 localhost >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: 无法连接到 localhost，请确保系统已正确配置
)

rem 切换到后端目录
set "backendPath=!scriptPath!simple-shop-backend\backend"
if not exist "!backendPath!" (
    echo 错误: 后端目录不存在: !backendPath!
    pause
    exit /b 1
)

cd /d "!backendPath!"
echo 已切换到后端目录: !cd!

rem 创建测试结果目录
set "reportPath=!backendPath!\target\surefire-reports"
if not exist "!reportPath!" (
    mkdir "!reportPath!" >nul 2>&1
    echo 已创建测试结果目录: !reportPath!
)

echo.
echo ========== 开始运行 Selenium 测试 ==========
echo 测试用例: AdminLoginTest.java, CustomerRegistrationTest.java, ProductManagementTest.java, CustomerManagementTest.java
echo.

rem 定义测试类列表
set "testClasses=^\
    com.shop.test.selenium.A.AdminLoginTest, ^\
    com.shop.test.selenium.A.CustomerRegistrationTest, ^\
    com.shop.test.selenium.A.ProductManagementTest, ^\
    com.shop.test.selenium.A.CustomerManagementTest"

rem 运行测试
set "testCmd=mvn test -Dtest=!testClasses!"
echo 执行命令: !testCmd!
echo.

!testCmd!

rem 检查测试结果
set "testExitCode=%errorlevel%"

if %testExitCode% equ 0 (
    echo.
    echo ========== 测试执行成功 ==========
) else (
    echo.
    echo ========== 测试执行失败 ==========
    echo 错误码: %testExitCode%
)

echo.
echo 测试结果已保存到: !reportPath!
echo 可以使用以下命令查看测试报告:
for %%f in ("!reportPath!\*.txt") do (
    echo  - type "%%f"
)

rem 提供运行特定测试的选项
echo.
echo 您可以使用以下命令运行特定的测试类:
echo 1. 运行管理员登录测试: mvn test -Dtest=com.shop.test.selenium.A.AdminLoginTest
echo 2. 运行客户注册测试: mvn test -Dtest=com.shop.test.selenium.A.CustomerRegistrationTest
echo 3. 运行商品管理测试: mvn test -Dtest=com.shop.test.selenium.A.ProductManagementTest
echo 4. 运行客户管理测试: mvn test -Dtest=com.shop.test.selenium.A.CustomerManagementTest

echo.
echo 按任意键退出...
pause >nul

exit /b %testExitCode%