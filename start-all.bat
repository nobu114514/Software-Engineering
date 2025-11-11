@echo off

rem 启用延迟环境变量扩展，用于在循环中正确获取errorlevel
setlocal enabledelayedexpansion

rem Pinduoduo Online Shopping System - One-click Startup Script (Local Mode)
echo ====================================================
echo              Pinduoduo Online Shopping System
echo              One-click Startup Script (Local Mode)
echo ====================================================

echo Checking system environment...

rem Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java environment not detected. Please install JDK 11 or higher.
    echo You can download from: https://adoptium.net/
    pause
    exit /b 1
)

rem Check if Node.js is installed
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Node.js environment not detected. Please install Node.js 14.x or higher.
    echo You can download from: https://nodejs.org/
    pause
    exit /b 1
)

rem Check if backend service is running
set "BACKEND_RUNNING=false"
netstat -ano | findstr :8081 >nul
if !errorlevel! equ 0 (
    set "BACKEND_RUNNING=true"
    echo Backend service may be running (port 8081)
)

rem Check if frontend service is running
set "FRONTEND_RUNNING=false"
netstat -ano | findstr :8080 >nul
if !errorlevel! equ 0 (
    set "FRONTEND_RUNNING=true"
    echo Frontend service may be running (port 8080)
)

echo.
echo Starting system components...

rem Start backend service
if "%BACKEND_RUNNING%" equ "false" (
    echo Starting backend service...
    start "Backend Service" cmd /c "cd simple-shop-backend\backend && java -Xms256m -Xmx512m -jar target\simple-shop-0.0.1-SNAPSHOT.jar"
    echo Backend service starting, please wait...
    
    rem Wait for backend service to start
    timeout /t 5 >nul
    echo Checking backend service status...
    
    rem Loop to check if backend service started successfully
    set "BACKEND_READY=false"
    for /L %%i in (1,1,10) do (
        timeout /t 3 >nul
        curl -s http://localhost:8081/actuator/health | findstr "UP" >nul
        if !errorlevel! equ 0 (
            set "BACKEND_READY=true"
            goto backend_ready
        )
        echo Backend service starting...(%%i/10)
    )
    
    :backend_ready
    if "%BACKEND_READY%" equ "true" (
        echo Backend service started successfully!
    ) else (
        echo Warning: Backend service may not have started completely. Please check http://localhost:8081/actuator/health later.
    )
) else (
    echo Backend service is already running, skipping startup
)

echo.

rem Start frontend service
if "%FRONTEND_RUNNING%" equ "false" (
    echo Starting frontend service...
    start "Frontend Service" cmd /c "cd simple-shop-frontend\frontend && npm run serve"
    echo Frontend service starting, please wait...
    
    rem Wait for frontend service to start
    timeout /t 5 >nul
    echo Checking frontend service status...
    
    rem Loop to check if frontend service started successfully
    set "FRONTEND_READY=false"
    for /L %%i in (1,1,10) do (
        timeout /t 3 >nul
        curl -s http://localhost:8080 | findstr "<!DOCTYPE html>" >nul
        if !errorlevel! equ 0 (
            set "FRONTEND_READY=true"
            goto frontend_ready
        )
        echo Frontend service starting...(%%i/10)
    )
    
    :frontend_ready
    if "%FRONTEND_READY%" equ "true" (
        echo Frontend service started successfully!
    ) else (
        echo Warning: Frontend service may not have started completely. Please check http://localhost:8080 later.
    )
) else (
    echo Frontend service is already running, skipping startup
)

echo.
echo ====================================================
echo System startup completed! Service access addresses:
echo. Frontend: http://localhost:8080
echo. Backend API: http://localhost:8081
echo. Health Check: http://localhost:8081/actuator/health
echo ====================================================
echo Notes:
echo. 1. Do not close the startup command windows
echo. 2. To stop services, close the corresponding command windows
echo. 3. Ensure MySQL database is running and 'simple_shop' database is created
echo ====================================================

set /p open_browser=Open browser to access the system? (y/n): 
if /i "%open_browser%" equ "y" (
    start http://localhost:8080
)

echo.
echo Press any key to exit...
pause >nul

rem End delayed environment variable expansion
endlocal