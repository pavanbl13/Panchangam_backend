@echo off
setlocal enabledelayedexpansion
cls

REM Read artifactId and version from pom.xml
set IMAGE_NAME=
set VERSION=
set "_ART_COUNT=0"
for /f "tokens=3 delims=<>" %%a in ('findstr "<artifactId>" pom.xml') do (
    set /a _ART_COUNT+=1
    if !_ART_COUNT! equ 2 set "IMAGE_NAME=%%a"
)
set "_VER_COUNT=0"
for /f "tokens=3 delims=<>" %%a in ('findstr "<version>" pom.xml') do (
    set /a _VER_COUNT+=1
    if !_VER_COUNT! equ 2 set "VERSION=%%a"
)
if not defined IMAGE_NAME set "IMAGE_NAME=sankalpam-api"
if not defined VERSION set "VERSION=latest"

echo.
echo ================================================================================
echo                   SANKALPAM API - DOCKER SETUP (SIMPLE)
echo                   Image: !IMAGE_NAME!:!VERSION!
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
set /p GEOSEARCH_API_KEY="Enter GEOSEARCH_API_KEY (get from https://myprojects.geoapify.com): "
if "!GEOSEARCH_API_KEY!"=="" (echo API key cannot be empty & pause & goto menu)
docker build -t !IMAGE_NAME!:!VERSION! .
docker stop !IMAGE_NAME! >nul 2>&1
docker rm !IMAGE_NAME! >nul 2>&1
echo.
echo Starting in DEBUG mode - Press Ctrl+C to stop
docker run -it --name !IMAGE_NAME! -p 8081:8081 -e GEOSEARCH_API_KEY=!GEOSEARCH_API_KEY! -v "%cd%\logs:/app/logs" !IMAGE_NAME!:!VERSION!
goto menu
:build_release
echo.
set /p GEOSEARCH_API_KEY="Enter GEOSEARCH_API_KEY (get from https://myprojects.geoapify.com): "
if "!GEOSEARCH_API_KEY!"=="" (echo API key cannot be empty & pause & goto menu)
echo Building image (this may take 10-15 minutes)...
docker build -t !IMAGE_NAME!:!VERSION! . >nul 2>&1
docker stop !IMAGE_NAME! >nul 2>&1
docker rm !IMAGE_NAME! >nul 2>&1
echo Starting container in background...
docker run -d --name !IMAGE_NAME! -p 8081:8081 -e GEOSEARCH_API_KEY=!GEOSEARCH_API_KEY! -v "%cd%\logs:/app/logs" --restart unless-stopped !IMAGE_NAME!:!VERSION!
echo Container started. API: http://localhost:8081
pause & goto menu
:run_debug
echo.
set /p GEOSEARCH_API_KEY="Enter GEOSEARCH_API_KEY: "
if "!GEOSEARCH_API_KEY!"=="" (echo API key cannot be empty & pause & goto menu)
docker stop !IMAGE_NAME! >nul 2>&1
docker rm !IMAGE_NAME! >nul 2>&1
docker run -it --name !IMAGE_NAME! -p 8081:8081 -e GEOSEARCH_API_KEY=!GEOSEARCH_API_KEY! -v "%cd%\logs:/app/logs" !IMAGE_NAME!:!VERSION!
goto menu
:logs
docker logs -f !IMAGE_NAME!
goto menu
:stop
docker stop !IMAGE_NAME!
pause & goto menu
:clean
set /p confirm="Delete container and images? (yes/no): "
if /i "%confirm%"=="yes" (docker stop !IMAGE_NAME! >nul 2>&1 & docker rm !IMAGE_NAME! >nul 2>&1 & docker rmi !IMAGE_NAME!:!VERSION! >nul 2>&1)
pause & goto menu
:end
endlocal
