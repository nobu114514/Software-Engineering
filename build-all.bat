@echo off

REM Simple and reliable build script

echo Building Shopping System...
echo ----------------------------------------

REM Create logs directory
mkdir logs 2>nul

echo Checking prerequisites...

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found! Please install JDK 11+
    pause
    exit /b 1
)

REM Build backend
cd simple-shop-backend\backend

echo Building backend...

REM Check if we can find Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo WARNING: Maven not found! Trying to continue...
)

REM Run Maven build
mvn clean package -DskipTests > ..\..\logs\backend-build.log 2>&1
if errorlevel 1 (
    echo ERROR: Backend build failed! Check logs\backend-build.log
    cd ..\..
    pause
    exit /b 1
)

echo Backend build successful!
cd ..\..

REM Build frontend
echo Building frontend...
cd simple-shop-frontend\frontend

REM Check Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo WARNING: Node.js not found! Skipping frontend build
    cd ..\..
    goto CHECK_BUILD
)

REM Install dependencies
echo Installing frontend dependencies...
npm install > ..\..\logs\frontend-install.log 2>&1
if errorlevel 1 (
    echo WARNING: Failed to install dependencies! Trying with mirror...
    npm install --registry=https://registry.npmmirror.com > ..\..\logs\frontend-install-mirror.log 2>&1
    if errorlevel 1 (
        echo ERROR: Failed to install dependencies! Check logs\frontend-install-mirror.log
        cd ..\..
        goto CHECK_BUILD
    )
)

REM Build frontend project
echo Building frontend...
npm run build > ..\..\logs\frontend-build.log 2>&1
if errorlevel 1 (
    echo ERROR: Frontend build failed! Check logs\frontend-build.log
    cd ..\..
    goto CHECK_BUILD
)

echo Frontend build successful!
cd ..\..

:CHECK_BUILD

REM Check build results
echo ----------------------------------------
echo Build Results:
echo ----------------------------------------

REM Check backend JAR
if exist simple-shop-backend\backend\target\simple-shop-0.0.1-SNAPSHOT.jar (
    echo ? Backend JAR: Found
) else (
    echo ? Backend JAR: Not found
)

REM Check frontend build
if exist simple-shop-frontend\frontend\dist\index.html (
    echo ? Frontend build: Found
) else (
    echo ? Frontend build: Not found
)

echo ----------------------------------------
echo Build completed!

echo.
echo Usage instructions:
echo - Run backend: java -jar simple-shop-backend\backend\target\simple-shop-0.0.1-SNAPSHOT.jar
echo - Run frontend dev: cd simple-shop-frontend\frontend && npm run serve
echo - Docker deploy: docker-compose up -d

echo.
pause