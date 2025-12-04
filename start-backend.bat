@echo off

rem Simple Shop Backend Startup Script
echo ====================================
echo     Simple Shop Backend Startup
echo ====================================

rem Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java not detected. Please install Java 11 or higher.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

rem Define JAR file path
set JAR_PATH=simple-shop-backend\backend\target\simple-shop-0.0.1-SNAPSHOT.jar

rem Check if JAR file exists
if not exist "%JAR_PATH%" (
    echo Error: JAR file not found: %JAR_PATH%
    echo Please run Maven build first: mvn clean package -DskipTests
    pause
    exit /b 1
)

rem Set JVM options
set JAVA_OPTS=-Xms256m -Xmx512m

echo Starting backend service...
echo Service URL: http://localhost:8081
echo Health Check: http://localhost:8081/actuator/health
echo Press Ctrl+C to stop

rem Start the application
java %JAVA_OPTS% -jar "%JAR_PATH%"