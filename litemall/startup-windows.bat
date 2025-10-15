@echo off

rem 拼夕夕在线购物系统 - Windows启动脚本
rem 此脚本用于在Windows环境下启动完整的应用系统

setlocal enabledelayedexpansion

echo ====================================================
echo              拼夕夕在线购物系统
 echo              Windows启动脚本
 echo ====================================================

rem 检查Docker是否已安装
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Docker，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    echo 或使用本地启动方式: .\start-all.bat
    pause
    goto use_local_startup
)

rem 检查docker-compose是否已安装
where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到docker-compose，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    echo 或使用本地启动方式: .\start-all.bat
    pause
    goto use_local_startup
)

rem 检查Docker服务是否正在运行
for /f "tokens=*" %%g in ('docker info') do (set DOCKER_STATUS=%%g && goto :break)
:break
if not defined DOCKER_STATUS (
    echo 错误: Docker服务未运行，请启动Docker Desktop
    echo 或使用本地启动方式: .\start-all.bat
    pause
    goto use_local_startup
)

echo 准备启动拼夕夕在线购物系统（Docker模式）...
echo ====================================================

rem 本地预先构建JAR包，避免在Docker中下载依赖
echo 0. 正在预构建后端JAR包...
cd simple-shop-backend\backend
mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 错误: 后端JAR包构建失败
    echo 尝试使用本地启动方式...
    cd ../..
    goto use_local_startup
)

cd ../..

echo 1. 正在拉取所需镜像...
docker-compose pull
if %errorlevel% neq 0 (
    echo 警告: 镜像拉取失败，可能是网络问题，尝试使用国内镜像源...
)

echo 2. 正在构建应用...
docker-compose build
if %errorlevel% neq 0 (
    echo 错误: Docker构建失败，可能是网络问题
    echo 尝试使用本地启动方式...
    goto use_local_startup
)

echo 3. 正在启动系统...
docker-compose up -d
if %errorlevel% equ 0 (
    echo.
echo ====================================================
echo 系统启动成功！
echo.    前端访问地址: http://localhost
echo.    后端API地址: http://localhost:8081
echo.    健康检查地址: http://localhost:8081/actuator/health
echo.    数据库地址: localhost:3306 (用户名: root, 密码: 2004, 数据库: simple_shop)
echo.
echo 查看日志命令: docker-compose logs -f
echo 停止系统命令: docker-compose down
echo ====================================================

    rem 询问是否打开浏览器
    set /p open_browser=是否打开浏览器访问系统？(y/n): 
    if /i "!open_browser!" equ "y" (
        start http://localhost
    )
) else (
    echo 系统启动失败，请检查错误信息
    echo 尝试使用本地启动方式...
    goto use_local_startup
)

pause
exit /b 0

:use_local_startup
rem 使用本地启动方案
echo ====================================================
echo 正在切换到本地启动模式...
echo ====================================================
start "拼夕夕在线购物系统 - 本地启动模式" cmd /c ".\start-all.bat"
exit /b 0