@echo off
REM 简单测试脚本

echo Hello World

REM 检查基本命令
java -version
if %ERRORLEVEL% NEQ 0 (
    echo Java not found
) else (
    echo Java found
)

REM 显示目录结构
dir simple-shop-backend
if %ERRORLEVEL% NEQ 0 (
    echo Backend directory not found
) else (
    echo Backend directory exists
)

dir simple-shop-frontend
if %ERRORLEVEL% NEQ 0 (
    echo Frontend directory not found
) else (
    echo Frontend directory exists
)

pause