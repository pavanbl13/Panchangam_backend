# Docker management script for Sankalpam API
# Provides easy commands for common Docker operations

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("build", "run", "stop", "logs", "status", "clean", "help")]
    [string]$Command,

    [string]$Mode = "debug",
    [string]$ImageTag = "latest",
    [switch]$Prod
)

# Configuration
$ImageName = if ($Prod) { "sankalpam-api-prod" } else { "sankalpam-api" }
$ContainerName = "sankalpam-api"
$Dockerfile = if ($Prod) { "Dockerfile.prod" } else { "Dockerfile" }
$Port = 8081

function Build-Image {
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "Building Docker Image" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "Image: $ImageName`:$ImageTag"
    Write-Host "Dockerfile: $Dockerfile"
    Write-Host ""

    docker build -t "${ImageName}:${ImageTag}" -f $Dockerfile .

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Image built successfully" -ForegroundColor Green
        Show-ImageInfo
    } else {
        Write-Host "[ERROR] Build failed" -ForegroundColor Red
        exit 1
    }
}

function Run-Container {
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "Starting Docker Container" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "Image: $ImageName`:$ImageTag"
    Write-Host "Mode: $Mode"
    Write-Host ""

    # Stop existing container
    Write-Host "Checking for existing container..." -ForegroundColor Yellow
    $existing = docker ps -aq --filter "name=$ContainerName" 2>$null
    if ($existing) {
        Write-Host "Stopping existing container..."
        docker stop $ContainerName | Out-Null
        docker rm $ContainerName | Out-Null
    }

    # Get Google API Key
    $ApiKey = $env:GOOGLE_API_KEY
    if (-not $ApiKey) {
        Write-Host "[WARNING] GOOGLE_API_KEY not set in environment" -ForegroundColor Yellow
        Write-Host "Please set it before running: `$env:GOOGLE_API_KEY='your-key'" -ForegroundColor Yellow
        $ApiKey = ""
    }

    if ($Mode -eq "debug") {
        Write-Host "Running in DEBUG mode (interactive)..." -ForegroundColor Cyan
        Write-Host "Press Ctrl+C to stop"
        Write-Host ""

        docker run -it `
            --name $ContainerName `
            -p "${Port}:${Port}" `
            -e "GOOGLE_API_KEY=$ApiKey" `
            -e "JAVA_OPTS=-Xmx512m -Xms256m" `
            -e "SPRING_PROFILES_ACTIVE=dev" `
            -v "${PWD}\logs:/app/logs" `
            "${ImageName}:${ImageTag}"
    } else {
        Write-Host "Running in RELEASE mode (background)..." -ForegroundColor Cyan

        docker run -d `
            --name $ContainerName `
            -p "${Port}:${Port}" `
            -e "GOOGLE_API_KEY=$ApiKey" `
            -e "JAVA_OPTS=-Xmx512m -Xms256m" `
            -e "SPRING_PROFILES_ACTIVE=prod" `
            -v "${PWD}\logs:/app/logs" `
            --restart unless-stopped `
            "${ImageName}:${ImageTag}"

        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] Container started successfully" -ForegroundColor Green
            Write-Host ""
            Write-Host "Container Name: $ContainerName" -ForegroundColor Cyan
            Write-Host "API URL: http://localhost:$Port" -ForegroundColor Cyan
            Write-Host "Health: http://localhost:$Port/actuator/health" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "Useful commands:" -ForegroundColor Yellow
            Write-Host "  View logs:    docker logs -f $ContainerName"
            Write-Host "  Stop:         docker stop $ContainerName"
            Write-Host "  Status:       docker ps | grep $ContainerName"
        } else {
            Write-Host "[ERROR] Failed to start container" -ForegroundColor Red
            exit 1
        }
    }
}

function Stop-Container {
    Write-Host "Stopping container: $ContainerName" -ForegroundColor Cyan

    $running = docker ps -q --filter "name=$ContainerName" 2>$null
    if ($running) {
        docker stop $ContainerName
        Write-Host "[OK] Container stopped" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Container is not running" -ForegroundColor Yellow
    }
}

function View-Logs {
    Write-Host "Viewing logs for: $ContainerName" -ForegroundColor Cyan
    Write-Host "(Press Ctrl+C to stop)" -ForegroundColor Yellow
    Write-Host ""

    docker logs -f $ContainerName
}

function Show-Status {
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "Docker Status" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host ""

    # Images
    Write-Host "AVAILABLE IMAGES:" -ForegroundColor Yellow
    docker images | grep sankalpam-api
    Write-Host ""

    # Running containers
    Write-Host "RUNNING CONTAINERS:" -ForegroundColor Yellow
    $running = docker ps | grep sankalpam-api
    if ($running) {
        Write-Host $running
    } else {
        Write-Host "No running containers" -ForegroundColor Gray
    }
    Write-Host ""

    # All containers
    Write-Host "ALL CONTAINERS:" -ForegroundColor Yellow
    docker ps -a | grep sankalpam-api
    Write-Host ""

    # Test health if running
    if (docker ps -q --filter "name=$ContainerName" 2>$null) {
        Write-Host "HEALTH CHECK:" -ForegroundColor Yellow
        try {
            $health = docker exec $ContainerName curl -s http://localhost:8081/actuator/health | ConvertFrom-Json
            Write-Host "Status: $($health.status)" -ForegroundColor Green
        } catch {
            Write-Host "Health check failed" -ForegroundColor Yellow
        }
    }
}

function Clean-Docker {
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "Cleaning Docker Resources" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host ""

    # Stop container
    if (docker ps -q --filter "name=$ContainerName" 2>$null) {
        Write-Host "Stopping container..."
        docker stop $ContainerName | Out-Null
    }

    # Remove container
    if (docker ps -aq --filter "name=$ContainerName" 2>$null) {
        Write-Host "Removing container..."
        docker rm $ContainerName | Out-Null
    }

    # Remove images
    Write-Host "Removing images..."
    docker rmi "sankalpam-api:$ImageTag" 2>$null
    docker rmi "sankalpam-api-prod:$ImageTag" 2>$null

    # Cleanup dangling images
    Write-Host "Cleaning up dangling images..."
    docker image prune -f | Out-Null

    Write-Host "[OK] Cleanup complete" -ForegroundColor Green
}

function Show-ImageInfo {
    Write-Host ""
    Write-Host "Image Details:" -ForegroundColor Yellow
    docker images "${ImageName}:${ImageTag}"
}

function Show-Help {
    Write-Host @"
================================================================================
                  DOCKER MANAGEMENT SCRIPT FOR SANKALPAM API
================================================================================

USAGE:
  .\docker-manage.ps1 -Command <command> [Options]

COMMANDS:
  build      Build Docker image
  run        Run Docker container
  stop       Stop Docker container
  logs       View container logs
  status     Show Docker status and health
  clean      Remove containers and images
  help       Show this help message

OPTIONS:
  -Mode      Run mode: 'debug' (default) or 'release'
  -ImageTag  Image tag (default: latest)
  -Prod      Use production image (Dockerfile.prod with Alpine)

EXAMPLES:
  # Build image
  .\docker-manage.ps1 -Command build

  # Build and run in debug mode
  .\docker-manage.ps1 -Command build
  .\docker-manage.ps1 -Command run -Mode debug

  # Build production image and run in release mode
  .\docker-manage.ps1 -Command build -Prod
  .\docker-manage.ps1 -Command run -Mode release -Prod

  # View logs
  .\docker-manage.ps1 -Command logs

  # Stop container
  .\docker-manage.ps1 -Command stop

  # Check status
  .\docker-manage.ps1 -Command status

  # Clean all resources
  .\docker-manage.ps1 -Command clean

REQUIREMENTS:
  - Docker Desktop installed
  - PowerShell v5.0+
  - Google API Key (set as environment variable: `$env:GOOGLE_API_KEY)

For more information, see DOCKER_README.md

================================================================================
"@
}

# Main execution
switch ($Command) {
    "build"   { Build-Image }
    "run"     { Run-Container }
    "stop"    { Stop-Container }
    "logs"    { View-Logs }
    "status"  { Show-Status }
    "clean"   { Clean-Docker }
    "help"    { Show-Help }
    default   { Show-Help }
}

