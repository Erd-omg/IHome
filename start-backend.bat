@echo off
chcp 65001 >nul
echo ========================================
echo Starting IHome Backend Service
========================================

cd backend

echo Checking Java environment...
java -version
if %errorlevel% neq 0 (
    echo Error: Java not installed or not configured
    pause
    exit /b 1
)

echo.
echo Checking Maven environment...
mvn -version
if %errorlevel% neq 0 (
    echo Error: Maven not installed or not configured
    pause
    exit /b 1
)

echo.
echo Building project (skip tests)...
mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo Error: Project build failed
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application (jar)...
echo Backend service will start at http://localhost:8080/api

echo.
java -jar target\ihome-0.0.1-SNAPSHOT.jar

pause
