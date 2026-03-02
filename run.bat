@echo off
setlocal enabledelayedexpansion
REM ====================================================================
REM Sankalpam API - Unified Build and Run Script
REM ====================================================================
REM Usage:
REM   run.bat                    - Run with defaults (build=true, debug=true)
REM   run.bat true false         - Build and run in release mode
REM   run.bat false true         - Skip build, run in debug mode
REM   run.bat /setup             - Interactive setup with API key prompt
REM   run.bat /setup true false  - Interactive setup then run with options
REM   run.bat your_api_key_here  - Auto-set API key and run with defaults
REM
REM Options:
REM   /setup       = Interactive mode (prompt for Geoapify API key and build/debug options)
REM   build        = true (default) | false
REM   debug        = true (default) | false
REM   API_KEY      = Geoapify API Key (can be passed as single argument)
REM ====================================================================
REM Default values
set BUILD=true
set DEBUG=true
set INTERACTIVE=false
REM Parse arguments
if not "%~1"=="" (
    if /i "%~1"=="/setup" (
        set INTERACTIVE=true
        REM Check if build/debug options follow /setup
        if not "%~2"=="" (
            if /i "%~2"=="true" set BUILD=true
            if /i "%~2"=="false" set BUILD=false
        )
        if not "%~3"=="" (
            if /i "%~3"=="true" set DEBUG=true
            if /i "%~3"=="false" set DEBUG=false
        )
    ) else (
        REM Check if first argument looks like an API key (longer string, no true/false)
        if /i not "%~1"=="true" if /i not "%~1"=="false" (
            set GEOSEARCH_API_KEY=%~1
            REM Parse second argument as build option
            if not "%~2"=="" (
                if /i "%~2"=="true" set BUILD=true
                if /i "%~2"=="false" set BUILD=false
            )
            REM Parse third argument as debug option
            if not "%~3"=="" (
                if /i "%~3"=="true" set DEBUG=true
                if /i "%~3"=="false" set DEBUG=false
            )
        ) else (
            REM First argument is true/false (build option)
            if /i "%~1"=="true" set BUILD=true
            if /i "%~1"=="false" set BUILD=false
            REM Parse second argument as debug option
            if not "%~2"=="" (
                if /i "%~2"=="true" set DEBUG=true
                if /i "%~2"=="false" set DEBUG=false
            )
        )
    )
)
color 0B
title Sankalpam API - Build and Run
REM ====================================================================
REM INTERACTIVE SETUP MODE (if /setup flag provided)
REM ====================================================================
if /i "!INTERACTIVE!"=="true" (
    echo.
    echo ================================================================================
    echo                    SANKALPAM API - SETUP AND RUN
    echo ================================================================================
    echo.
    REM Prompt for Geoapify API key
    echo [INFO] Geoapify API key is required for city search and geocoding
    echo.
    echo Enter your Geoapify API key:
    set /p GEOSEARCH_API_KEY="Geoapify API Key (or leave blank to continue): "
    if defined GEOSEARCH_API_KEY (
        echo [SUCCESS] GEOSEARCH_API_KEY environment variable set
    ) else (
        echo [WARNING] No API key provided - City search and geocoding may not work
        echo.
        echo To get your API key:
        echo   1. Go to https://myprojects.geoapify.com/
        echo   2. Create a new project
        echo   3. Copy the API key
        echo.
    )
    echo.
    echo Build application? [Y/n]:
    set /p buildResponse="Enter choice: "
    if /i "!buildResponse!"=="n" (
        set BUILD=false
    ) else (
        set BUILD=true
    )
    echo.
    echo Debug mode? [Y/n]:
    set /p debugResponse="Enter choice: "
    if /i "!debugResponse!"=="n" (
        set DEBUG=false
    ) else (
        set DEBUG=true
    )
    echo.
) else (
    echo.
    echo ================================================================================
    echo                      SANKALPAM API - BUILD AND RUN
    echo ================================================================================
    echo.
)
echo Build: !BUILD!
echo Debug: !DEBUG!
echo.
REM Check if GEOSEARCH_API_KEY is set
if defined GEOSEARCH_API_KEY (
    echo GEOSEARCH_API_KEY: Configured
) else (
    echo GEOSEARCH_API_KEY: NOT SET - City search and geocoding may not work
    echo [WARNING] Please set GEOSEARCH_API_KEY before running the application
    echo.
    echo To set it, you can:
    echo   Option 1 - In Command Prompt, run:
    echo     set GEOSEARCH_API_KEY=your_api_key_here
    echo.
    echo   Option 2 - Run this script with API key as argument:
    echo     run.bat your_api_key_here
    echo.
    echo   Option 3 - Run this script in interactive setup mode:
    echo     run.bat /setup
    echo.
)
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
    start "Sankalpam API - Application" /MAX cmd /c "color 0A && title Sankalpam API - Port 8081 && cd /d "%~dp0" && set GEOSEARCH_API_KEY=!GEOSEARCH_API_KEY! && java -jar target\sankalpam-api-1.0.0.jar"
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
