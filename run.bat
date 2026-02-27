@echo off
setlocal enabledelayedexpansion

REM ====================================================================
REM Sankalpam API - Build and Run with Options
REM ====================================================================
REM Usage: run.bat [build] [debug]
REM   build: true (default) = build | false = skip build
REM   debug: true (default) = debug mode (2 windows) | false = release (1 window)
REM ====================================================================

REM Default values
set BUILD=true
set DEBUG=true

REM Parse first argument (build option)
if not "%~1"=="" (
    if /i "%~1"=="true" set BUILD=true
    if /i "%~1"=="false" set BUILD=false
)

REM Parse second argument (debug option)
if not "%~2"=="" (
    if /i "%~2"=="true" set DEBUG=true
    if /i "%~2"=="false" set DEBUG=false
)

color 0B
title Sankalpam API - Build and Run

echo.
echo ================================================================================
echo                      SANKALPAM API - BUILD AND RUN
echo ================================================================================
echo.
echo Build: !BUILD!
echo Debug: !DEBUG!
echo.
echo ================================================================================
echo.

cd /d "%~dp0"

REM ====================================================================
REM STEP 1: BUILD (if enabled)
REM ====================================================================

if /i "!BUILD!"=="true" (
    echo [INFO] Step 1: Building application with Maven...
    echo.

    call mvn clean install -DskipTests

    if !ERRORLEVEL! neq 0 (
        echo.
        echo [ERROR] Build failed!
        echo.
        pause
        exit /b 1
    )

    echo.
    echo [SUCCESS] Build completed successfully!
    echo.

    if not exist "target\sankalpam-api-1.0.0.jar" (
        echo [ERROR] JAR file not found!
        echo.
        pause
        exit /b 1
    )
) else (
    echo [INFO] Step 1: Skipping build. Using existing JAR...
    echo.

    if not exist "target\sankalpam-api-1.0.0.jar" (
        echo [ERROR] JAR file not found at target\sankalpam-api-1.0.0.jar
        echo [INFO] Please run with build=true first
        echo.
        pause
        exit /b 1
    )
)

REM ====================================================================
REM STEP 2: PREPARE TO RUN
REM ====================================================================

echo [INFO] Step 2: Preparing to start application...
echo.

if not exist "logs" mkdir logs

echo [INFO] Checking if port 8081 is available...
netstat -ano | findstr :8081
if !ERRORLEVEL! equ 0 (
    echo [WARNING] Port 8081 is already in use!
    echo [INFO] Stopping process on port 8081...
    taskkill /F /IM java.exe
    timeout /t 2 /nobreak >nul
    echo [INFO] Process stopped
)

echo [INFO] Port 8081 is ready. Proceeding...
echo.

REM ====================================================================
REM STEP 3: RUN APPLICATION
REM ====================================================================

if /i "!DEBUG!"=="true" (
    echo [INFO] Step 3: Starting application in DEBUG mode
    echo [INFO] Application will open in a separate window
    echo.

    start "Sankalpam API - Application" /MAX cmd /c "color 0A && title Sankalpam API - Port 8081 && cd /d "%~dp0" && java -jar target\sankalpam-api-1.0.0.jar"

    echo [SUCCESS] Application started!
    echo.
    echo Open the log file in your terminal to monitor:
    echo   Type: tail -f logs/application-runtime.log
    echo Or use: Get-Content logs\application-runtime.log -Wait
    echo.

) else (
    echo [INFO] Step 3: Starting application in RELEASE mode
    echo.
    java -jar target\sankalpam-api-1.0.0.jar
)

