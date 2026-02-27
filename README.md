README - Running the Sankalpam API
==================================
To provide sankalpam for everyday

QUICK START
===========

The easiest way to run the application is using the run.bat file with options.

USAGE
=====

run.bat [build] [debug]

Where:
- build  : true (default) = Build before running | false = Run existing JAR
- debug  : true (default) = Debug mode (2 windows) | false = Release mode (1 window)

EXAMPLES
========

Default (Build + Debug Mode):
> run.bat

Build with debug mode:
> run.bat true true

Build without debug mode:
> run.bat true false

Use existing JAR with debug:
> run.bat false true

Use existing JAR without debug:
> run.bat false false

Shorthand (same as above):
> run.bat true false      (Build + Release)
> run.bat false           (No build + Debug)
> run.bat false false     (No build + Release)

MODES EXPLAINED
===============

BUILD MODE
----------
true (default)  : Runs mvn clean install -DskipTests
                 Duration: ~9 seconds
                 Recompiles all code, skips tests

false           : Skips build step
                 Starts application immediately using existing JAR
                 Faster if you just want to run

DEBUG MODE
----------
true (default)  : Opens 1 external window
                 Application runs in separate CMD window (green)
                 Logs written to file: logs/application-runtime.log
                 View logs in current terminal using:
                   PowerShell: Get-Content logs\application-runtime.log -Wait
                   PowerShell: tail -f logs/application-runtime.log (if using Git Bash)
                 Perfect for development and testing with Postman

false           : Opens 1 window
                 Single console with all output
                 Simpler setup, good for production testing

WHAT HAPPENS
============

When you run "run.bat":

Step 1: BUILD (if build=true)
  └─ Executes: mvn clean install -DskipTests
  └─ Checks: If JAR file was created
  └─ Duration: ~9 seconds

Step 2: PREPARE
  └─ Creates: logs directory (if not exists)
  └─ Checks: If port 8081 is available
  └─ Action: Kills existing Java process if port is in use
  └─ Shows: Netstat output directly in console

Step 3: RUN

  If debug=true:
    └─ Opens: 1 external CMD window (green) with Java application
    └─ Logs: Written to logs/application-runtime.log
    └─ View logs: In current terminal using Get-Content or tail command
    └─ Files: temp_log_viewer.ps1 is NOT created

  If debug=false:
    └─ Runs: Java application in current window
    └─ Shows: All output in single console

APPLICATION READY WHEN
======================

You'll see messages like:
[INFO] Step 3: Starting application in DEBUG mode
[INFO] Application will open in a separate window
[SUCCESS] Application started!

Or in the external CMD window:
Tomcat started on port(s): 8081 (http)
Started SankalpamApplication in X.XXX seconds

Monitor logs in your current terminal with:
  PowerShell: Get-Content logs\application-runtime.log -Wait
  Git Bash:   tail -f logs/application-runtime.log

ACCESSING THE API
=================

Once running, you can:

Health Check:
  curl http://localhost:8081/actuator/health
  OR
  Browser: http://localhost:8081/actuator/health

Test API:
  Use Postman to POST to: http://localhost:8081/api/find
  
  Body:
  {
    "fullName": "Test User",
    "city": "New York",
    "date": "2026-02-24",
    "time": "18:30"
  }

STOPPING THE APPLICATION
=========================

In Debug Mode (1 external window + terminal logs):
  - Close the external CMD window
  OR
  - Press Ctrl+C in the external CMD window
  
In Release Mode (single window):
  - Press Ctrl+C in console
  OR
  - Close the console window

To stop log monitoring in terminal:
  - Press Ctrl+C while viewing logs with Get-Content or tail

TROUBLESHOOTING
===============

Q: "Build failed" error
A: Check Maven is installed (mvn --version)
   Check Java 21+ is installed (java -version)
   Check internet for downloading dependencies

Q: "Port 8081 is already in use" warning
A: This is normal!
   Script automatically kills the existing process
   If it doesn't work, manually kill: taskkill /F /IM java.exe

Q: Application won't start (both windows open but nothing runs)
A: Check console window for error messages
   Check logs/application-runtime.log
   Ensure Google API key is configured in application.yml

Q: No logs appearing in debug window
A: Wait a few seconds after startup
   Check that logs directory was created
   Verify log file exists: logs/application-runtime.log

Q: "Unexpected error: Failed to fetch coordinates"
A: Google API key not configured properly
   Check application.yml
   Verify Google Geocoding API is enabled in Google Cloud

DEVELOPMENT WORKFLOW
====================

Typical Development:
  1. Open IntelliJ terminal
  2. Make code changes
  3. Run: run.bat true true
     (Build with debug to see logs)
  4. In another terminal tab: Get-Content logs\application-runtime.log -Wait
  5. Test in Postman or browser
  6. Watch logs in terminal as requests come in
  7. Fix issues, repeat

Quick Testing:
  1. Use: run.bat false true
     (Skip build, just run with debug)
  2. Faster startup
  3. Monitor logs in terminal

Production Testing:
  1. Use: run.bat true false
     (Build with release mode)
  2. Single console output
  3. Better for monitoring performance

CONFIGURATION
=============

Before first run, configure:

File: src/main/resources/application.yml

Required:
  google.api.key: YOUR_API_KEY_HERE

Optional:
  server.port: 8081 (default)
  logging.level.com.sankalpam: DEBUG (default: INFO)

Get Google API Key:
  1. Go to: https://console.cloud.google.com
  2. Create new project
  3. Enable: Geocoding API
  4. Enable: Time Zone API
  5. Create: API key
  6. Add to application.yml

BATCH FILE OPTIONS EXPLAINED
=============================

The run.bat script accepts parameters in order:

Position 1: BUILD
  - Value: true | false (or blank for default)
  - Default: true
  - Effect: Whether to run mvn clean install

Position 2: DEBUG
  - Value: true | false (or blank for default)
  - Default: true
  - Effect: Whether to open dual view or single window

EXAMPLES WITH EXPLANATIONS:

run.bat
  → build=true (default), debug=true (default)
  → Builds AND opens 2 windows

run.bat false
  → build=false, debug=true (default)
  → No build, opens 2 windows

run.bat true false
  → build=true, debug=false
  → Builds AND opens 1 window

run.bat false false
  → build=false, debug=false
  → No build, opens 1 window

run.bat true
  → build=true, debug=true (default)
  → Builds AND opens 2 windows (same as just "run.bat")

ADVANCED USAGE
==============

Using Maven Directly:

Build only (no run):
  mvn clean install -DskipTests

Run JAR directly:
  java -jar target/sankalpam-api-1.0.0.jar

Run JAR on different port:
  java -jar target/sankalpam-api-1.0.0.jar --server.port=9000

LOGS LOCATION
=============

Real-time logs displayed in log viewer window when running in debug mode

Persistent logs saved to:
  logs/application-runtime.log

Log format:
  HH:MM:SS.mmm [THREAD] LEVEL LOGGER_NAME - MESSAGE

Example:
  13:25:48.123 [main] INFO com.sankalpam.SankalpamApplication - Tomcat started on port(s): 8081

PLATFORM REQUIREMENTS
====================

Operating System: Windows (uses .bat file)

Required Software:
  - Java 21 or newer
  - Maven 3.8 or newer
  - Internet connection (for dependencies and Google APIs)

Optional:
  - Postman (for API testing)
  - Text editor (for editing application.yml)

CHECK YOUR SETUP
================

Verify Java:
  java -version
  (Should show version 21 or higher)

Verify Maven:
  mvn --version
  (Should show Maven 3.8 or higher)

If either is missing, install from:
  Java: https://www.oracle.com/java/technologies/downloads/
  Maven: https://maven.apache.org/download.cgi

NEXT STEPS
==========

1. Configure Google API key in application.yml
2. Run: run.bat
3. Wait for "Application started successfully"
4. Open Postman or browser to http://localhost:8081/actuator/health
5. Test the /api/find endpoint with sample data
6. Check logs in log viewer window (debug mode)

For full API documentation, see: API_DOCUMENTATION.md

