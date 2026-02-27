# Docker Deployment Guide - Sankalpam API

Complete documentation for building, deploying, and maintaining the Sankalpam API using Docker.

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Setup Instructions](#setup-instructions)
4. [Docker Images](#docker-images)
5. [Building Docker Image](#building-docker-image)
6. [Running Container](#running-container)
7. [Docker Compose](#docker-compose)
8. [Testing and Verification](#testing-and-verification)
9. [Cloud Deployment (Render.com)](#cloud-deployment-rendercom)
10. [Quick Reference](#quick-reference)
11. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Windows Batch Script (Easiest)
```cmd
cd C:\Family\Pavan\AI\Panchangam\sankalpam-project\backend
set GOOGLE_API_KEY=your-actual-api-key-here
docker-build-run.bat
```

### PowerShell Script
```powershell
$env:GOOGLE_API_KEY="your-actual-api-key-here"
.\docker-manage.ps1 -Command build
.\docker-manage.ps1 -Command run -Mode debug
```

### Docker Compose (Simple)
```cmd
set GOOGLE_API_KEY=your-actual-api-key-here
docker-compose up --build
```

---

## Prerequisites

### System Requirements
- **Docker Desktop** (Windows/Mac) or Docker Engine (Linux)
  - Download: https://www.docker.com/products/docker-desktop
  - Minimum version: 20.10+
- **Docker Compose** (usually included with Docker Desktop)
- **At least 2GB free disk space** for image
- **Google Geolocation API Key** from https://console.cloud.google.com

### Verify Installation
```cmd
docker --version
docker-compose --version
docker run hello-world
```

---

## Setup Instructions

### 1. Set Google API Key

#### Windows Command Prompt
```cmd
set GOOGLE_API_KEY=your-actual-api-key-here
echo %GOOGLE_API_KEY%
```

#### Windows PowerShell
```powershell
$env:GOOGLE_API_KEY="your-actual-api-key-here"
echo $env:GOOGLE_API_KEY
```

#### Linux/Mac
```bash
export GOOGLE_API_KEY="your-actual-api-key-here"
echo $GOOGLE_API_KEY
```

### 2. Navigate to Project
```cmd
cd C:\Family\Pavan\AI\Panchangam\sankalpam-project\backend
```

### 3. Choose Your Method (See sections below)

---

## Docker Images

### Standard Image
- **Dockerfile:** `Dockerfile`
- **Base:** eclipse-temurin:21-jre
- **Size:** ~350MB
- **Startup:** 4-6 seconds
- **Memory:** 150-200MB at runtime
- **Use:** Development and general deployment

### Production Image (Alpine)
- **Dockerfile:** `Dockerfile.prod`
- **Base:** eclipse-temurin:21-jre-alpine
- **Size:** ~120MB
- **Startup:** 4-6 seconds
- **Memory:** 150-200MB at runtime
- **Use:** Cloud deployment, size-constrained environments
- **Security:** Non-root user, minimal attack surface

### Image Comparison

| Feature | Standard | Production |
|---------|----------|-----------|
| Base | JRE | Alpine JRE |
| Size | 350MB | 120MB |
| Security | root user | non-root user |
| Performance | Standard | Optimized (G1GC) |
| Startup | 4-6s | 4-6s |

---

## Building Docker Image

### Standard Build
```cmd
docker build -t sankalpam-api:latest .
```

### Production Build (Alpine - Smaller)
```cmd
docker build -t sankalpam-api:prod -f Dockerfile.prod .
```

### Verify Image Built
```cmd
docker images | findstr sankalpam-api
```

Expected output:
```
REPOSITORY          TAG       IMAGE ID      CREATED        SIZE
sankalpam-api       latest    1234567890    2 min ago      350MB
sankalpam-api       prod      0987654321    1 min ago      120MB
```

### Build Progress
The build will show:
```
Step 1/10 : FROM maven:3.9-eclipse-temurin-21 AS builder
Step 2/10 : WORKDIR /app
...
Step 10/10 : CMD ["sh", "-c", "java $JAVA_OPTS -jar sankalpam-api-1.0.0.jar"]
Successfully built 1234567890ab
Successfully tagged sankalpam-api:latest
```

---

## Running Container

### Method 1: Debug Mode (Interactive - View Logs)
```cmd
docker run -it ^
    --name sankalpam-api ^
    -p 8081:8081 ^
    -e GOOGLE_API_KEY=%GOOGLE_API_KEY% ^
    -v %cd%\logs:/app/logs ^
    sankalpam-api:latest
```

Press Ctrl+C to stop.

### Method 2: Release Mode (Background)
```cmd
docker run -d ^
    --name sankalpam-api ^
    -p 8081:8081 ^
    -e GOOGLE_API_KEY=%GOOGLE_API_KEY% ^
    -v %cd%\logs:/app/logs ^
    --restart unless-stopped ^
    sankalpam-api:latest
```

### Method 3: Using Batch Script
```cmd
docker-build-run.bat              (Build + Debug)
docker-build-run.bat --nobuild    (Use existing image)
docker-build-run.bat --release    (Release mode)
docker-build-run.bat --prod       (Production image)
```

### Method 4: Using PowerShell Script
```powershell
.\docker-manage.ps1 -Command run -Mode debug
.\docker-manage.ps1 -Command run -Mode release
.\docker-manage.ps1 -Command run -Mode release -Prod
```

### Verify Container Running
```cmd
docker ps | findstr sankalpam-api
```

Expected:
```
1234567890ab   sankalpam-api:latest   4 hours  Up 2 min  0.0.0.0:8081->8081/tcp
```

---

## Docker Compose

### Quick Start
```cmd
set GOOGLE_API_KEY=your-api-key
docker-compose up --build
```

### Build and Run in Background
```cmd
docker-compose up -d --build
```

### View Logs
```cmd
docker-compose logs -f
```

### Stop Services
```cmd
docker-compose down
```

### Remove Volumes (Clean Everything)
```cmd
docker-compose down -v
```

### Other Commands
```cmd
docker-compose ps              (List services)
docker-compose logs            (Show all logs)
docker-compose logs -f service (Follow specific service)
docker-compose exec container cmd  (Run command in container)
```

---

## Testing and Verification

### Verification Checklist

- [ ] Docker is installed and running
- [ ] Google API Key is set
- [ ] Image built successfully
- [ ] Container starts without errors
- [ ] API endpoint responds
- [ ] Logs are visible and persistent
- [ ] Health check passes
- [ ] Multiple locations tested

### Step 1: Test Docker Installation
```cmd
docker --version
docker run hello-world
```

### Step 2: Build Image
```cmd
docker build -t sankalpam-api:latest .
```

### Step 3: Run Container
```cmd
set GOOGLE_API_KEY=your-api-key
docker run -it ^
    --name sankalpam-api ^
    -p 8081:8081 ^
    -e GOOGLE_API_KEY=%GOOGLE_API_KEY% ^
    -v %cd%\logs:/app/logs ^
    sankalpam-api:latest
```

Expected startup logs:
```
Started SankalpamApplication in 5.123 seconds
INFO com.sankalpam.SankalpamApplication - Application started successfully
```

### Step 4: Test Health Endpoint

**PowerShell:**
```powershell
Invoke-RestMethod -Uri http://localhost:8081/actuator/health | ConvertTo-Json
```

**Expected response:**
```json
{
  "status": "UP"
}
```

### Step 5: Test API Endpoint

**PowerShell:**
```powershell
$body = @{
    fullName = "Test User"
    city = "New York"
    date = "2026-02-27"
    time = "18:30"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8081/api/find" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$response | ConvertTo-Json -Depth 10
```

**Expected response:**
```json
{
  "success": true,
  "message": "Sankalpam found successfully",
  "data": {
    "samvatsaram": "viSvAvasu",
    "ayanam": "uttarAyaNE",
    "ruthuvu": "Shishira Ruthu",
    "maasam": "Phalgunamu",
    "paksham": "Shukla Paksham",
    "tithi": "dasamyAm",
    "vaaram": "Guru Vaaram",
    "nakshatram": "Ardra",
    "sunrise": "06:44:19",
    "sunset": "19:31:51",
    "validUntil": "06:27:24 PM"
  },
  "timestamp": "2026-02-27T12:34:56.123456Z"
}
```

### Step 6: Check Logs
```cmd
docker logs sankalpam-api
docker logs -f sankalpam-api    (Follow logs in real-time)
```

### Step 7: Monitor Performance
```cmd
docker stats sankalpam-api
```

Shows: Memory usage, CPU percentage, Network I/O, Block I/O

### Step 8: Test with Different Locations

**Sydney, Australia:**
```powershell
$body = @{
    fullName = "Test"
    city = "Sydney"
    date = "2026-02-27"
    time = "19:00"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/find" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body | ConvertTo-Json
```

---

## Cloud Deployment (Render.com)

### Method 1: GitHub Integration (Recommended - Automatic)

1. **Ensure Dockerfile is in repository root** ✓
2. **Push code to GitHub:**
   ```cmd
   git add .
   git commit -m "Add Docker configuration"
   git push origin main
   ```

3. **On Render Dashboard:**
   - Click "New +" → "Web Service"
   - Connect GitHub repository
   - Render auto-detects Dockerfile
   - Configure settings:
     - **Name:** `sankalpam-api`
     - **Runtime:** Docker
     - **Region:** Choose nearest region
   - Add Environment Variable:
     - **Key:** `GOOGLE_API_KEY`
     - **Value:** Your actual API key
   - Click "Create Web Service"

4. **Deployment:**
   - Render clones repository
   - Builds Docker image
   - Pushes to Render registry
   - Starts container
   - Assigns public HTTPS URL
   - Takes 2-3 minutes

### Method 2: From Docker Hub (Manual)

1. **Build and push to Docker Hub:**
   ```cmd
   REM Login to Docker Hub
   docker login
   
   REM Tag image
   docker tag sankalpam-api:latest yourusername/sankalpam-api:latest
   
   REM Push
   docker push yourusername/sankalpam-api:latest
   ```

2. **On Render Dashboard:**
   - Click "New +" → "Web Service"
   - Select "Docker" as source
   - Enter image URL: `yourusername/sankalpam-api:latest`
   - Configure environment and deploy

### Testing Production Deployment

Once deployed on Render:
```powershell
$apiUrl = "https://your-render-url/api/find"

$body = @{
    fullName = "Test"
    city = "New York"
    date = "2026-02-27"
    time = "18:30"
} | ConvertTo-Json

Invoke-RestMethod -Uri $apiUrl `
    -Method POST `
    -ContentType "application/json" `
    -Body $body | ConvertTo-Json
```

---

## Quick Reference

### Build Commands
```cmd
docker build -t sankalpam-api:latest .              (Standard)
docker build -t sankalpam-api:prod -f Dockerfile.prod .  (Production)
docker images | findstr sankalpam-api               (List images)
```

### Run Commands
```cmd
REM Debug mode
docker run -it -p 8081:8081 -e GOOGLE_API_KEY=%GOOGLE_API_KEY% sankalpam-api:latest

REM Release mode
docker run -d -p 8081:8081 -e GOOGLE_API_KEY=%GOOGLE_API_KEY% sankalpam-api:latest
```

### Container Management
```cmd
docker ps                          (List running)
docker ps -a                       (List all)
docker stop sankalpam-api          (Stop)
docker start sankalpam-api         (Start)
docker restart sankalpam-api       (Restart)
docker rm sankalpam-api            (Remove)
docker rm -f sankalpam-api         (Force remove)
docker inspect sankalpam-api       (Details)
```

### Logging
```cmd
docker logs sankalpam-api          (Show logs)
docker logs -f sankalpam-api       (Follow logs)
docker logs --tail 50 sankalpam-api   (Last 50 lines)
docker logs --since 5m sankalpam-api  (Last 5 minutes)
```

### Testing
```cmd
docker stats sankalpam-api         (Resource usage)
docker exec sankalpam-api curl http://localhost:8081/actuator/health (Health check)
docker port sankalpam-api          (Show port mappings)
```

### Cleanup
```cmd
docker stop sankalpam-api
docker rm sankalpam-api
docker rmi sankalpam-api:latest
docker system prune                (Remove unused)
docker system prune -a             (Remove all unused)
```

### Script Commands
```cmd
docker-build-run.bat --help        (Show help)
docker-build-run.bat               (Build + debug)
docker-build-run.bat --nobuild     (Use existing)
docker-build-run.bat --release     (Release mode)
docker-build-run.bat --prod        (Production image)

.\docker-manage.ps1 -Command build (Build)
.\docker-manage.ps1 -Command run   (Run)
.\docker-manage.ps1 -Command logs  (View logs)
.\docker-manage.ps1 -Command stop  (Stop)
.\docker-manage.ps1 -Command status (Show status)
.\docker-manage.ps1 -Command clean (Clean resources)
```

---

## Troubleshooting

### Docker not found
**Problem:** "Command 'docker' not found"
**Solution:**
- Install Docker Desktop from https://www.docker.com/products/docker-desktop
- Restart terminal after installation
- Verify: `docker --version`

### Port already in use
**Problem:** "bind: address already in use :8081"
**Solution:**
```cmd
REM Check what's using the port
netstat -ano | findstr :8081

REM Use different port
docker run -p 8082:8081 sankalpam-api:latest

REM Or stop existing container
docker stop sankalpam-api
docker rm sankalpam-api
```

### Google API key not working
**Problem:** "Failed to fetch coordinates" error
**Solution:**
```cmd
REM Verify key is set
echo %GOOGLE_API_KEY%

REM Verify key is passed to container
docker inspect sankalpam-api | findstr GOOGLE_API_KEY

REM Restart with correct key
docker stop sankalpam-api
docker rm sankalpam-api
docker run -e GOOGLE_API_KEY=your-key sankalpam-api:latest
```

### Image not found
**Problem:** "no such image"
**Solution:**
```cmd
docker build -t sankalpam-api:latest .
docker images | findstr sankalpam-api
```

### Container exits immediately
**Problem:** "container stops without logs"
**Solution:**
```cmd
REM Run in foreground to see errors
docker run -it sankalpam-api:latest

REM Check logs
docker logs sankalpam-api
```

### Out of memory
**Problem:** "Java.lang.OutOfMemoryError"
**Solution:**
```cmd
REM Increase JVM heap
docker run -e JAVA_OPTS="-Xmx1g -Xms512m" sankalpam-api:latest

REM Check memory usage
docker stats sankalpam-api
```

### Connection refused
**Problem:** "curl: (7) Failed to connect to localhost:8081"
**Solution:**
```cmd
REM Verify container is running
docker ps | findstr sankalpam-api

REM Check port mapping
docker inspect sankalpam-api | findstr -A 5 PortBindings

REM Try wait for startup (takes 4-6 seconds)
timeout /t 5
curl http://localhost:8081/actuator/health
```

### Build fails
**Problem:** "Docker build error"
**Solution:**
```cmd
REM Build with verbose output
docker build --progress=plain -t sankalpam-api:latest .

REM Check pom.xml exists
dir pom.xml

REM Check Dockerfile exists
dir Dockerfile

REM Clear Docker cache and rebuild
docker build --no-cache -t sankalpam-api:latest .
```

---

## Environment Variables

### Required
- `GOOGLE_API_KEY` - Google Geolocation API Key (from Google Cloud Console)
  - **Security:** Must be passed at runtime via `-e` flag, NEVER in Dockerfile
  - **Why:** Secrets baked into images are visible to anyone with access to the image
  - **How:** `docker run -e GOOGLE_API_KEY=your-key ...`

### Optional
- `JAVA_OPTS` - JVM options (default: `-Xmx512m -Xms256m`)
- `SPRING_PROFILES_ACTIVE` - Spring profile (default: `prod`)
- `SERVER_PORT` - Server port (default: `8081`)

### Security Best Practices

1. **Never hardcode secrets in Dockerfile**
   ```dockerfile
   # ❌ BAD - Exposes secret in image
   ENV GOOGLE_API_KEY=secret-key-12345
   
   # ✅ GOOD - Pass at runtime
   # Note: GOOGLE_API_KEY must be passed at runtime via -e flag
   ```

2. **Pass secrets at runtime**
   ```cmd
   docker run -e GOOGLE_API_KEY=%GOOGLE_API_KEY% sankalpam-api:latest
   ```

3. **Use secure secret management for production**
   - Docker Secrets (Swarm)
   - Kubernetes Secrets (K8s)
   - Cloud provider secret managers (AWS, Azure, GCP)
   - Environment file with restricted permissions

### Example Usage (SECURE)
```cmd
REM Set in environment first
set GOOGLE_API_KEY=your-key

REM Pass to container (key is NOT in image)
docker run -d ^
    -p 8081:8081 ^
    -e GOOGLE_API_KEY=%GOOGLE_API_KEY% ^
    -e JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC" ^
    -e SPRING_PROFILES_ACTIVE=prod ^
    sankalpam-api:latest
```

### Example Usage (Docker Compose - SECURE)
```yaml
version: '3.8'
services:
  sankalpam-api:
    image: sankalpam-api:latest
    environment:
      GOOGLE_API_KEY: ${GOOGLE_API_KEY}  # Loaded from system env var
      JAVA_OPTS: "-Xmx1g -Xms512m"
```

Then pass the key:
```cmd
set GOOGLE_API_KEY=your-key
docker-compose up
```

---

## Performance Optimization

### Memory Configuration
```cmd
REM Conservative (256MB heap)
-e JAVA_OPTS="-Xmx512m -Xms256m"

REM Standard (512MB heap)
-e JAVA_OPTS="-Xmx1g -Xms512m"

REM High performance (1GB heap + G1GC)
-e JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"
```

### Startup Time Measurement
```powershell
$timer = [System.Diagnostics.Stopwatch]::StartNew()
docker run -it sankalpam-api:latest
$timer.Stop()
Write-Host "Startup time: $($timer.ElapsedMilliseconds)ms"
```

Expected: 4-6 seconds

### Response Time Measurement
```powershell
$timer = [System.Diagnostics.Stopwatch]::StartNew()
$response = Invoke-RestMethod -Uri "http://localhost:8081/api/find" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"fullName":"Test","city":"New York","date":"2026-02-27","time":"18:30"}'
$timer.Stop()
Write-Host "Response time: $($timer.ElapsedMilliseconds)ms"
```

Expected: 200-500ms

### Image Size Optimization
```cmd
REM Standard image
docker build -t sankalpam-api:latest .
REM Size: ~350MB

REM Production image (Alpine)
docker build -t sankalpam-api:prod -f Dockerfile.prod .
REM Size: ~120MB (65% smaller)
```

---

## Files Reference

| File | Purpose | Lines |
|------|---------|-------|
| `Dockerfile` | Standard multi-stage build | 32 |
| `Dockerfile.prod` | Production Alpine build | 45 |
| `.dockerignore` | Build context filter | 47 |
| `docker-compose.yml` | Docker Compose config | 42 |
| `docker-build-run.bat` | Windows batch script | 180+ |
| `docker-manage.ps1` | PowerShell script | 280+ |

---

## Summary

✅ **Docker implementation is COMPLETE and READY**

You now have:
- ✓ Production-ready Dockerfiles (standard + Alpine)
- ✓ Docker Compose configuration
- ✓ Windows automation scripts (batch + PowerShell)
- ✓ Comprehensive testing procedures
- ✓ Cloud deployment instructions
- ✓ Performance optimization guidelines
- ✓ Complete troubleshooting guide

### Next Steps:
1. Read Quick Start section above
2. Set GOOGLE_API_KEY environment variable
3. Run `docker-build-run.bat` or `docker-compose up --build`
4. Test with API endpoints
5. Deploy to Render.com (optional)

---

**Last Updated:** February 27, 2026
**Status:** ✅ COMPLETE AND READY FOR DEPLOYMENT

