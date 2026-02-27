@echo off
REM ==============================================================================
REM Docker Build and Run Script for Sankalpam API
REM ==============================================================================
REM This script builds and runs the Sankalpam API as a Docker container
REM ==============================================================================

setlocal enabledelayedexpansion

REM Default values
set BUILD_IMAGE=true
set RUN_MODE=debug
set IMAGE_NAME=sankalpam-api
set IMAGE_TAG=latest
set CONTAINER_NAME=sankalpam-api
set PORT=8081

:parse_args
if "%~1"=="" goto start_process
if /i "%~1"=="--nobuild" set BUILD_IMAGE=false & shift & goto parse_args
if /i "%~1"=="--release" set RUN_MODE=release & shift & goto parse_args
if /i "%~1"=="--debug" set RUN_MODE=debug & shift & goto parse_args
if /i "%~1"=="--prod" (
    set IMAGE_NAME=sankalpam-api-prod
    set IMAGE_TAG=latest
    shift
    goto parse_args
)
if /i "%~1"=="--help" goto show_help
shift
goto parse_args

:show_help
echo.
echo Usage: docker-build-run.bat [OPTIONS]
echo.
echo OPTIONS:
echo   --nobuild      Skip building Docker image (use existing image)
echo   --debug        Run in debug mode (default)
echo   --release      Run in release mode
echo   --prod         Use production image (Dockerfile.prod with Alpine)
echo   --help         Show this help message
echo.
echo EXAMPLES:
echo   docker-build-run.bat                    (Build + Run in debug mode)
echo   docker-build-run.bat --nobuild --release (Use existing image + release mode)
echo   docker-build-run.bat --prod --release   (Build prod image + release mode)
echo.
pause
goto end

:start_process
cls
echo ================================================================================
echo                  DOCKER BUILD AND RUN SCRIPT
echo ================================================================================
echo.

REM Check if Docker is installed
echo [INFO] Checking Docker installation...
docker --version > nul 2>&1
if errorlevel 1 (
    color 0C
    echo [ERROR] Docker is not installed or not in PATH
    echo Please install Docker from: https://www.docker.com/products/docker-desktop
    pause
    goto end
)

echo [OK] Docker is installed
echo.

REM Step 1: Check/Build Image
if /i "%BUILD_IMAGE%"=="true" (
    echo [INFO] Step 1: Building Docker image...
    echo.

    set DOCKERFILE=Dockerfile
    if /i "%IMAGE_NAME%"=="sankalpam-api-prod" (
        set DOCKERFILE=Dockerfile.prod
        echo [INFO] Using production Dockerfile (Alpine base)
    ) else (
        echo [INFO] Using standard Dockerfile (JRE base)
    )

    docker build -t !IMAGE_NAME!:!IMAGE_TAG! -f !DOCKERFILE! .
    if errorlevel 1 (
        color 0C
        echo [ERROR] Docker build failed
        pause
        goto end
    )
    echo [OK] Docker image built successfully: !IMAGE_NAME!:!IMAGE_TAG!
) else (
    echo [INFO] Step 1: Skipping image build (using existing image)
    docker images | findstr "!IMAGE_NAME!" > nul
    if errorlevel 1 (
        color 0C
        echo [ERROR] Image !IMAGE_NAME! not found. Build it first with --build
        pause
        goto end
    )
)

echo.

REM Step 2: Stop existing container if running
echo [INFO] Step 2: Checking for existing container...
docker ps -a --filter name=!CONTAINER_NAME! --quiet > nul 2>&1
if not errorlevel 1 (
    echo [INFO] Stopping existing container !CONTAINER_NAME!...
    docker stop !CONTAINER_NAME! > nul 2>&1
    docker rm !CONTAINER_NAME! > nul 2>&1
    echo [OK] Previous container removed
) else (
    echo [INFO] No existing container found
)

echo.

REM Step 3: Run container
echo [INFO] Step 3: Starting Docker container...
echo [INFO] Mode: !RUN_MODE!
echo [INFO] Port: !PORT!
echo.

REM Check if GOOGLE_API_KEY is set
if "!GOOGLE_API_KEY!"=="" (
    echo [WARNING] GOOGLE_API_KEY environment variable not set
    echo [WARNING] Please set GOOGLE_API_KEY before running the API
    echo [INFO] You can set it in PowerShell: $env:GOOGLE_API_KEY="your-key"
    echo.
)

REM Run container with appropriate settings
if /i "%RUN_MODE%"=="debug" (
    echo [INFO] Running in DEBUG mode with interactive terminal
    echo [INFO] Press Ctrl+C to stop the container
    echo.
    docker run -it ^
        --name !CONTAINER_NAME! ^
        -p !PORT!:!PORT! ^
        -e GOOGLE_API_KEY=!GOOGLE_API_KEY! ^
        -e JAVA_OPTS="-Xmx512m -Xms256m" ^
        -e SPRING_PROFILES_ACTIVE="dev" ^
        -v "%cd%\logs:/app/logs" ^
        !IMAGE_NAME!:!IMAGE_TAG!
) else (
    echo [INFO] Running in RELEASE mode (background)
    docker run -d ^
        --name !CONTAINER_NAME! ^
        -p !PORT!:!PORT! ^
        -e GOOGLE_API_KEY=!GOOGLE_API_KEY! ^
        -e JAVA_OPTS="-Xmx512m -Xms256m" ^
        -e SPRING_PROFILES_ACTIVE="prod" ^
        -v "%cd%\logs:/app/logs" ^
        --restart unless-stopped ^
        !IMAGE_NAME!:!IMAGE_TAG!

    if errorlevel 1 (
        color 0C
        echo [ERROR] Failed to start container
        pause
        goto end
    )

    echo [OK] Container started successfully
    echo.
    echo [INFO] Container ID: !CONTAINER_NAME!
    echo [INFO] API URL: http://localhost:!PORT!
    echo [INFO] Health Check: http://localhost:!PORT!/actuator/health
    echo.
    echo [INFO] View logs with: docker logs -f !CONTAINER_NAME!
    echo [INFO] Stop container with: docker stop !CONTAINER_NAME!
)

echo.
echo [SUCCESS] Process completed!
echo.

:end
endlocal

