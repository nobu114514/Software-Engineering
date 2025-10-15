@echo off

rem 拼夕夕在线购物系统 - 一键启动脚本（不依赖Docker）
echo ====================================================
echo              拼夕夕在线购物系统
 echo              一键启动脚本（本地模式）
 echo ====================================================

rem 设置颜色
echo [92m正在检查系统环境...[0m

rem 检查Java是否已安装
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [91m错误: 未检测到Java环境，请先安装JDK 11或更高版本[0m
    echo 您可以从以下地址下载: https://adoptium.net/
    pause
    exit /b 1
)

rem 检查Node.js是否已安装
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [91m错误: 未检测到Node.js环境，请先安装Node.js 14.x或更高版本[0m
    echo 您可以从以下地址下载: https://nodejs.org/
    pause
    exit /b 1
)

rem 检查是否有后端服务在运行
set "BACKEND_RUNNING=false"
netstat -ano | findstr :8081 >nul
if %errorlevel% equ 0 (
    set "BACKEND_RUNNING=true"
    echo [93m检测到后端服务可能已在运行（端口8081）[0m
)

rem 检查是否有前端服务在运行
set "FRONTEND_RUNNING=false"
netstat -ano | findstr :8080 >nul
if %errorlevel% equ 0 (
    set "FRONTEND_RUNNING=true"
    echo [93m检测到前端服务可能已在运行（端口8080）[0m
)

echo.
echo [92m开始启动系统组件...[0m

rem 启动后端服务
if "%BACKEND_RUNNING%" equ "false" (
    echo [92m正在启动后端服务...[0m
    start "后端服务" cmd /c "cd simple-shop-backend\backend && java -Xms256m -Xmx512m -jar target\simple-shop-0.0.1-SNAPSHOT.jar"
    echo 后端服务启动中，请稍候...
    
    rem 等待后端服务启动
    timeout /t 5 >nul
    echo 检查后端服务状态...
    
    rem 循环检查后端服务是否启动成功
    set "BACKEND_READY=false"
    for /L %%i in (1,1,10) do (
        timeout /t 3 >nul
        curl -s http://localhost:8081/actuator/health | findstr "UP" >nul
        if %errorlevel% equ 0 (
            set "BACKEND_READY=true"
            goto backend_ready
        )
        echo 后端服务正在启动中...(%%i/10)
    )
    
    :backend_ready
    if "%BACKEND_READY%" equ "true" (
        echo [92m后端服务启动成功！[0m
    ) else (
        echo [93m警告: 后端服务可能未完全启动，请稍后检查 http://localhost:8081/actuator/health[0m
    )
) else (
    echo 后端服务已在运行，跳过启动
)

echo.

rem 启动前端服务
if "%FRONTEND_RUNNING%" equ "false" (
    echo [92m正在启动前端服务...[0m
    start "前端服务" cmd /c "cd simple-shop-frontend\frontend && npm run serve"
    echo 前端服务启动中，请稍候...
    
    rem 等待前端服务启动
    timeout /t 5 >nul
    echo 检查前端服务状态...
    
    rem 循环检查前端服务是否启动成功
    set "FRONTEND_READY=false"
    for /L %%i in (1,1,10) do (
        timeout /t 3 >nul
        curl -s http://localhost:8080 | findstr "<!DOCTYPE html>" >nul
        if %errorlevel% equ 0 (
            set "FRONTEND_READY=true"
            goto frontend_ready
        )
        echo 前端服务正在启动中...(%%i/10)
    )
    
    :frontend_ready
    if "%FRONTEND_READY%" equ "true" (
        echo [92m前端服务启动成功！[0m
    ) else (
        echo [93m警告: 前端服务可能未完全启动，请稍后检查 http://localhost:8080[0m
    )
) else (
    echo 前端服务已在运行，跳过启动
)

echo.
echo ====================================================
echo [92m系统启动完成！以下是服务访问地址：[0m
echo. 前端页面: http://localhost:8080
echo. 后端API: http://localhost:8081
echo. 健康检查: http://localhost:8081/actuator/health
echo ====================================================
echo [93m注意事项：[0m
echo. 1. 请勿关闭启动的命令窗口
 echo. 2. 如需停止服务，请直接关闭对应的命令窗口
 echo. 3. 确保MySQL数据库正在运行，且已创建simple_shop数据库
 echo ====================================================

rem 打开浏览器访问前端页面
set /p open_browser=是否打开浏览器访问系统？(y/n): 
if /i "%open_browser%" equ "y" (
    start http://localhost:8080
)

echo.
echo 按任意键退出...
pause >nul