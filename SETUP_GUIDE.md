# Sankalpam API - Setup & Running Guide

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- Windows OS (batch file provided)

## Environment Variables Setup

### Geoapify API Key
Before running the application, set the Geoapify API key as an environment variable:

**PowerShell:**
```powershell
$env:GEOSEARCH_API_KEY = "your_geoapify_api_key_here"
```

**Command Prompt (CMD):**
```cmd
set GEOSEARCH_API_KEY=your_geoapify_api_key_here
```

**Permanent (Windows):**
1. Open System Properties (Win + X, then select "System")
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Click "New" under User variables
5. Variable name: `GEOSEARCH_API_KEY`
6. Variable value: `your_geoapify_api_key_here`
7. Click OK and restart applications

**Get your API key:**
1. Go to https://myprojects.geoapify.com/
2. Create a new project
3. Copy the API key

## Building and Running

### Option 1: Using provided batch file (Recommended)
Run the consolidated build and launch script:

```cmd
run.bat
```

This will:
1. Run `mvn clean install -DskipTests`
2. Start the Spring Boot application
3. Display application logs in real-time

### Option 2: Manual build and run

**Build the application:**
```cmd
mvn clean install -DskipTests
```

**Run the application:**
```cmd
java -jar target/sankalpam-api-1.0.0.jar
```

## Application Access

Once running, the API is available at:
- **Base URL:** `http://localhost:8081`
- **API Endpoint:** `http://localhost:8081/api/find`

## Testing with Postman

Import the Postman collection from:
```
postman/Sankalpam_API_v2.postman_collection.json
```

### Example Request:
```json
POST http://localhost:8081/api/find
Content-Type: application/json

{
  "fullName": "Test User",
  "city": "New York",
  "date": "2026-02-24",
  "time": "18:30"
}
```

## Logs

Application logs are written to:
- **File:** `logs/application-runtime.log`
- **Console:** Real-time output during execution

## Security Notes

- **Never commit API keys** to the repository
- Always use environment variables for sensitive credentials
- The `application.yml` file is configured to read from `${GEOSEARCH_API_KEY}` environment variable
- Additional environment-specific configs can be added to `.gitignore` for local development

## Troubleshooting

### Port 8081 already in use
```cmd
netstat -ano | findstr :8081
```
Kill the process using the port or change the port in `application.yml`

### API Key not found error
Ensure the `GEOSEARCH_API_KEY` environment variable is set before starting the application.

### Build failures
Clear Maven cache:
```cmd
mvn clean install -DskipTests -U
```

## Project Structure

```
backend/
├── src/main/
│   ├── java/com/sankalpam/
│   │   ├── controller/     (REST endpoints)
│   │   ├── service/        (Business logic)
│   │   ├── model/          (Data models)
│   │   ├── dto/            (Data transfer objects)
│   │   ├── util/           (Utilities)
│   │   ├── exception/      (Exception handlers)
│   │   └── config/         (Configuration classes)
│   └── resources/
│       ├── application.yml (Main configuration)
│       ├── data/           (Mapping data files)
│       └── lookup/         (JSON lookup files)
├── pom.xml                 (Maven configuration)
├── run.bat                 (Build & run batch file)
└── docs/                   (Documentation)
```
