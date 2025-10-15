@echo off

rem 拼夕夕在线购物系统 - Windows启动脚本
rem 此脚本用于在Windows环境下启动完整的应用系统

echo 拼夕夕在线购物系统启动脚本
setlocal enabledelayedexpansion

rem 检查Docker是否已安装
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Docker，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

rem 检查docker-compose是否已安装
where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到docker-compose，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

rem 检查Docker服务是否正在运行
for /f "tokens=*" %%g in ('docker info') do (set DOCKER_STATUS=%%g && goto :break)
:break
if not defined DOCKER_STATUS (
    echo 错误: Docker服务未运行，请启动Docker Desktop
    pause
    exit /b 1
)

echo 准备启动拼夕夕在线购物系统...
echo 1. 正在拉取所需镜像...
docker-compose pull
echo 2. 正在构建应用...
docker-compose build
echo 3. 正在启动系统...
docker-compose up -d

if %errorlevel% equ 0 (
    echo.
    echo 系统启动成功！
    echo.    前端访问地址: http://localhost
    echo.    后端API地址: http://localhost:8081
    echo.    数据库地址: localhost:3306 (用户名: root, 密码: root, 数据库: simple_shop)
    echo.
    echo 查看日志命令: docker-compose logs -f
    echo 停止系统命令: docker-compose down
    echo.
) else (
    echo 系统启动失败，请检查错误信息
)

pause