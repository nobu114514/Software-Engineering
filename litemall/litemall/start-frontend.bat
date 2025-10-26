@echo off

rem Simple Shop Frontend Service Startup Script (Docker Independent)
echo ====================================
echo     Simple Shop Frontend Startup
 echo ====================================

rem Check if Node.js is installed
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Node.js not detected, please install Node.js first
    echo You can download from: https://nodejs.org/
    pause
    exit /b 1
)

rem Check if npm is installed
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: npm not detected, please install Node.js first
    pause
    exit /b 1
)

rem Navigate to frontend project directory
echo Navigating to frontend project directory...
cd simple-shop-frontend\frontend || (
    echo Error: Frontend project directory not found
    pause
    exit /b 1
)

rem Check if node_modules exists
if exist node_modules (
    echo Detected existing dependencies, skipping npm install
) else (
    rem Install dependencies
    echo Installing dependencies...
    npm install --legacy-peer-deps || (
        echo Error: Failed to install dependencies
        pause
        exit /b 1
    )
)

rem Start development server
echo Starting frontend development server...
echo Service URL: http://localhost:8080
 echo Backend API: http://localhost:8081
 echo Press Ctrl+C to stop service
echo ====================================

npm run serve