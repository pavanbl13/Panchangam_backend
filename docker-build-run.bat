@echo off
setlocal enabledelayedexpansion
REM Docker build, tag, login, and push script

REM ====================================================================
REM Read artifactId and version from pom.xml
REM ====================================================================
set IMAGE_NAME=
set VERSION=

REM Parse artifactId: first <artifactId> tag in pom.xml that is NOT inside <parent>
REM We use a simple approach: find lines with <artifactId> and pick the second one
REM (first belongs to the parent block)
set "_ART_COUNT=0"
for /f "tokens=3 delims=<>" %%a in ('findstr "<artifactId>" pom.xml') do (
    set /a _ART_COUNT+=1
    if !_ART_COUNT! equ 2 set "IMAGE_NAME=%%a"
)

REM Parse version: second <version> tag in pom.xml
REM (1st = parent version, 2nd = project version)
set "_VER_COUNT=0"
for /f "tokens=3 delims=<>" %%a in ('findstr "<version>" pom.xml') do (
    set /a _VER_COUNT+=1
    if !_VER_COUNT! equ 2 set "VERSION=%%a"
)

if not defined IMAGE_NAME set "IMAGE_NAME=sankalpam-api"
if not defined VERSION set "VERSION=latest"

echo [INFO] Detected from pom.xml: !IMAGE_NAME!:!VERSION!
echo.

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
