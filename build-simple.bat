@echo off

REM Very simple build script

echo Building Project...

REM Check directories
dir
echo.
echo Backend directory check:
if exist simple-shop-backend\backend (
    echo Backend directory exists
) else (
    echo Backend directory NOT found
)

echo.
echo Frontend directory check:
if exist simple-shop-frontend\frontend (
    echo Frontend directory exists
) else (
    echo Frontend directory NOT found
)

echo.
echo Java version check:
java -version

echo.
echo Build script completed.
pause