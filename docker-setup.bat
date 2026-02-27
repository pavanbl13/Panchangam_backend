@echo off
setlocal enabledelayedexpansion
cls
echo.
echo ================================================================================
echo                   SANKALPAM API - DOCKER SETUP (SIMPLE)
echo ================================================================================
echo.
:menu
cls
echo [1] Build and run in DEBUG mode (see live logs)
echo [2] Build and run in RELEASE mode (background)
echo [3] Run existing image (DEBUG mode)
echo [4] View logs of running container
echo [5] Stop container
echo [6] Clean up
echo [7] Exit
echo.
set /p choice="Select option (1-7): "
if "%choice%"=="1" goto build_debug
if "%choice%"=="2" goto build_release
if "%choice%"=="3" goto run_debug
if "%choice%"=="4" goto logs
if "%choice%"=="5" goto stop
if "%choice%"=="6" goto clean
if "%choice%"=="7" goto end
goto menu
:build_debug
echo.
set /p GOOGLE_API_KEY="Enter GOOGLE_API_KEY (get from https://console.cloud.google.com): "
if "!GOOGLE_API_KEY!"=="" (echo API key cannot be empty & pause & goto menu)
docker build -t sankalpam-api:latest .
docker stop sankalpam-api >nul 2>&1
docker rm sankalpam-api >nul 2>&1
echo.
echo Starting in DEBUG mode - Press Ctrl+C to stop
docker run -it --name sankalpam-api -p 8081:8081 -e GOOGLE_API_KEY=!GOOGLE_API_KEY! -v "%cd%\logs:/app/logs" sankalpam-api:latest
goto menu
:build_release
echo.
set /p GOOGLE_API_KEY="Enter GOOGLE_API_KEY (get from https://console.cloud.google.com): "
if "!GOOGLE_API_KEY!"=="" (echo API key cannot be empty & pause & goto menu)
echo Building image (this may take 10-15 minutes)...
docker build -t sankalpam-api:latest . >nul 2>&1
docker stop sankalpam-api >nul 2>&1
docker rm sankalpam-api >nul 2>&1
echo Starting container in background...
docker run -d --name sankalpam-api -p 8081:8081 -e GOOGLE_API_KEY=!GOOGLE_API_KEY! -v "%cd%\logs:/app/logs" --restart unless-stopped sankalpam-api:latest
echo Container started. API: http://localhost:8081
pause & goto menu
:run_debug
echo.
set /p GOOGLE_API_KEY="Enter GOOGLE_API_KEY: "
if "!GOOGLE_API_KEY!"=="" (echo API key cannot be empty & pause & goto menu)
docker stop sankalpam-api >nul 2>&1
docker rm sankalpam-api >nul 2>&1
docker run -it --name sankalpam-api -p 8081:8081 -e GOOGLE_API_KEY=!GOOGLE_API_KEY! -v "%cd%\logs:/app/logs" sankalpam-api:latest
goto menu
:logs
docker logs -f sankalpam-api
goto menu
:stop
docker stop sankalpam-api
pause & goto menu
:clean
set /p confirm="Delete container and images? (yes/no): "
if /i "%confirm%"=="yes" (docker stop sankalpam-api >nul 2>&1 & docker rm sankalpam-api >nul 2>&1 & docker rmi sankalpam-api:latest >nul 2>&1)
pause & goto menu
:end
endlocal
