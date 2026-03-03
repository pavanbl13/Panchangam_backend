@echo off
setlocal enabledelayedexpansion
REM ====================================================================
REM Sankalpam - Unified Build and Run Script
REM ====================================================================
REM Builds the React frontend, copies it into Spring Boot static
REM resources, builds the backend JAR, and runs the unified app.
REM
REM Usage:
REM   run.bat                    - Full build (frontend + backend) and run
REM   run.bat --skip-fe          - Skip frontend build (reuse last dist)
REM   run.bat --skip-build       - Skip all builds (reuse existing JAR)
REM   run.bat /setup             - Interactive setup (API key + options)
REM   run.bat your_api_key_here  - Set API key and run with defaults
REM
REM Logs open automatically in a separate window.
REM ====================================================================

cd /d "%~dp0"

REM ── Read artifactId and version from pom.xml ──
set "ARTIFACT_ID="
set "APP_VERSION="
set "_ART_COUNT=0"
for /f "tokens=3 delims=<>" %%a in ('findstr "<artifactId>" pom.xml') do (
    set /a _ART_COUNT+=1
    if !_ART_COUNT! equ 2 set "ARTIFACT_ID=%%a"
)
set "_VER_COUNT=0"
for /f "tokens=3 delims=<>" %%a in ('findstr "<version>" pom.xml') do (
    set /a _VER_COUNT+=1
    if !_VER_COUNT! equ 2 set "APP_VERSION=%%a"
)
if not defined ARTIFACT_ID set "ARTIFACT_ID=sankalpam-api"
if not defined APP_VERSION set "APP_VERSION=1.0.0"
set "JAR_FILE=!ARTIFACT_ID!-!APP_VERSION!.jar"

set "FRONTEND_DIR=%~dp0src\main\frontend"
set "STATIC_DIR=%~dp0src\main\resources\static"

REM ── Default values ──
set BUILD_FE=true
set BUILD_BE=true
set INTERACTIVE=false

REM ── Parse arguments ──
:parse_args
if "%~1"=="" goto args_done
if /i "%~1"=="/setup"       ( set INTERACTIVE=true& shift & goto parse_args )
if /i "%~1"=="--skip-fe"    ( set BUILD_FE=false& shift & goto parse_args )
if /i "%~1"=="--skip-build" ( set BUILD_FE=false& set BUILD_BE=false& shift & goto parse_args )
REM If arg is not a flag, treat it as an API key
if /i not "%~1"=="true" if /i not "%~1"=="false" (
    set GEOSEARCH_API_KEY=%~1
)
shift
goto parse_args
:args_done

color 0B
title Sankalpam - Build and Run

REM ====================================================================
REM INTERACTIVE SETUP (if /setup flag provided)
REM ====================================================================
if /i "!INTERACTIVE!"=="true" (
    echo.
    echo ================================================================================
    echo                    SANKALPAM - SETUP AND RUN
    echo ================================================================================
    echo.
    echo [INFO] Geoapify API key is required for city search and geocoding
    echo.
    set /p GEOSEARCH_API_KEY="Geoapify API Key (or leave blank to continue): "
    if defined GEOSEARCH_API_KEY (
        echo [SUCCESS] GEOSEARCH_API_KEY set
    ) else (
        echo [WARNING] No API key - city search may not work
    )
    echo.
    echo Build frontend? [Y/n]:
    set /p feResponse="Enter choice: "
    if /i "!feResponse!"=="n" set BUILD_FE=false
    echo.
    echo Build backend? [Y/n]:
    set /p beResponse="Enter choice: "
    if /i "!beResponse!"=="n" set BUILD_BE=false
    echo.
)

echo.
echo ================================================================================
echo                    SANKALPAM - UNIFIED APP
echo ================================================================================
echo.
echo   Frontend build : !BUILD_FE!
echo   Backend build  : !BUILD_BE!
if defined GEOSEARCH_API_KEY (
    echo   API Key        : Configured
) else (
    echo   API Key        : NOT SET
    echo.
    echo   [WARNING] Set GEOSEARCH_API_KEY before running:
    echo     set GEOSEARCH_API_KEY=your_key   then   run.bat
    echo     run.bat your_key
    echo     run.bat /setup
)
echo.
echo ================================================================================
echo.

REM ====================================================================
REM STEP 1: BUILD REACT FRONTEND
REM ====================================================================
if /i "!BUILD_FE!"=="true" (
    echo [1/4] Building React frontend...
    echo.
    cd /d "!FRONTEND_DIR!"
    if not exist "node_modules" (
        echo       Installing npm dependencies...
        call npm ci
        if !ERRORLEVEL! neq 0 (
            echo [ERROR] npm ci failed. Ensure Node.js is installed.
            pause & exit /b 1
        )
    )
    echo       Running vite build...
    call npm run build
    if !ERRORLEVEL! neq 0 (
        echo [ERROR] Frontend build failed.
        pause & exit /b 1
    )
    echo       Frontend build complete.
    echo.
    cd /d "%~dp0"
) else (
    echo [1/4] Skipping frontend build ^(--skip-fe^)
    echo.
)

REM ====================================================================
REM STEP 2: COPY FRONTEND TO STATIC RESOURCES
REM ====================================================================
if /i "!BUILD_FE!"=="true" (
    echo [2/4] Copying frontend to static resources...
    if exist "!STATIC_DIR!" rmdir /s /q "!STATIC_DIR!"
    mkdir "!STATIC_DIR!"
    xcopy "!FRONTEND_DIR!\dist\*" "!STATIC_DIR!\" /s /e /q /y > nul
    echo       Copied to src\main\resources\static\
    echo.
) else (
    echo [2/4] Using existing static resources
    if not exist "!STATIC_DIR!\index.html" (
        echo [ERROR] No frontend build found in !STATIC_DIR!
        echo [INFO]  Run without --skip-fe first to build the frontend.
        pause & exit /b 1
    )
    echo.
)

REM ====================================================================
REM STEP 3: BUILD SPRING BOOT BACKEND
REM ====================================================================
cd /d "%~dp0"
if /i "!BUILD_BE!"=="true" (
    echo [3/4] Building Spring Boot backend...
    echo.
    call mvn clean package -DskipTests -Dfrontend.skip=true
    if !ERRORLEVEL! neq 0 (
        echo [ERROR] Maven build failed!
        pause & exit /b 1
    )
    echo.
    echo       Backend build complete.
    echo.
) else (
    echo [3/4] Skipping backend build ^(--skip-build^)
    echo.
)

REM Verify JAR exists
if not exist "target\!JAR_FILE!" (
    echo [ERROR] JAR not found: target\!JAR_FILE!
    echo [INFO]  Run without --skip-build to build first.
    pause & exit /b 1
)

REM ====================================================================
REM STEP 4: RUN APPLICATION
REM ====================================================================
echo [4/4] Starting application...
echo.
if not exist "logs" mkdir logs

REM Clear old log file for fresh session
if exist "logs\application-runtime.log" del /q "logs\application-runtime.log"

REM Check if port 8000 is in use
netstat -ano | findstr ":8000 " | findstr LISTENING > nul 2>&1
if !ERRORLEVEL! equ 0 (
    echo [WARNING] Port 8000 is already in use!
    echo [INFO] Attempting to stop existing process...
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8000 " ^| findstr LISTENING') do (
        taskkill /F /PID %%p > nul 2>&1
    )
    timeout /t 2 /nobreak > nul
)

REM Start the application in a separate window
start "Sankalpam App" /MAX cmd /c "color 0A && title Sankalpam - Port 8000 && cd /d "%~dp0" && set GEOSEARCH_API_KEY=!GEOSEARCH_API_KEY! && java -jar target\!JAR_FILE!"

REM Wait for the log file to appear
echo       Waiting for application to start...
set /a WAIT_COUNT=0
:wait_for_log
if exist "logs\application-runtime.log" goto log_ready
timeout /t 1 /nobreak > nul
set /a WAIT_COUNT+=1
if !WAIT_COUNT! gtr 15 goto log_ready
goto wait_for_log
:log_ready

REM Open a log viewer in a separate window
echo       Opening log viewer...
start "Sankalpam Logs" cmd /c "color 0E && title Sankalpam - Live Logs && cd /d "%~dp0" && echo Waiting for logs... && timeout /t 3 /nobreak > nul && powershell -Command \"Get-Content 'logs\application-runtime.log' -Wait -Tail 100\""

echo.
echo ================================================================================
echo   Sankalpam is starting on http://localhost:8000
echo ================================================================================
echo.
echo   [App Window]  Green window  - Application output
echo   [Log Window]  Yellow window - Live log stream
echo.
echo   Press any key to close this window (app keeps running)...
echo.
pause > nul
