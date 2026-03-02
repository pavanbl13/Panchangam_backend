@echo off
REM Docker build, tag, login, and push script

REM Set variables
set IMAGE_NAME=sankalpam-api
set VERSION=1.0.0
set DOCKER_USERNAME=pavanbl
set REGISTRY=docker.io

REM Step 1: Build the Docker image
echo Building Docker image...
docker build -t %IMAGE_NAME%:%VERSION% .
if %errorlevel% neq 0 (
    echo Error: Docker build failed
    exit /b 1
)
echo Docker build completed successfully

REM Step 2: Tag the image
echo Tagging image...
docker tag %IMAGE_NAME%:%VERSION% %DOCKER_USERNAME%/%IMAGE_NAME%:%VERSION%
if %errorlevel% neq 0 (
    echo Error: Docker tag failed
    exit /b 1
)
echo Image tagged successfully

REM Step 3: Docker login
echo Logging in to Docker Hub...
echo Please enter your Docker Hub username: %DOCKER_USERNAME%
echo When prompted, enter your Docker Hub password:
docker login -u %DOCKER_USERNAME%
if %errorlevel% neq 0 (
    echo Error: Docker login failed
    echo Please verify your username and password are correct
    exit /b 1
)
echo Docker login successful

REM Step 4: Push the image
echo Pushing image to Docker Hub...
docker push %DOCKER_USERNAME%/%IMAGE_NAME%:%VERSION%
if %errorlevel% neq 0 (
    echo Error: Docker push failed
    exit /b 1
)
echo Docker push completed successfully

echo.
echo All operations completed successfully!
echo Image pushed to: %DOCKER_USERNAME%/%IMAGE_NAME%:%VERSION%
