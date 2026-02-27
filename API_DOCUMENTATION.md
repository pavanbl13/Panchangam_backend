Sankalpam API - Complete Documentation
=======================================

PROJECT OVERVIEW
================

The Sankalpam API is a Spring Boot REST API that provides Hindu religious calendar calculations and Panchang (lunar calendar) information for any given date, time, and geographical location.

WHAT IS SANKALPAM?
==================

Sankalpam is a Sanskrit term meaning "intention" or "resolution". In Hindu tradition, a Sankalpam is a formal declaration made before performing a religious ritual or ceremony. It typically includes:
- The name of the person
- The date and time of the ceremony
- The location where the ceremony is being performed
- Astrological details (Samvatsara, Ayana, Ruthu, Masa, Paksha, Tithi, Vaara, Nakshatra)

API PURPOSE
===========

This API calculates and returns detailed Panchang (Hindu calendar) information based on:
- User's full name
- City/Location (automatically fetches geographical coordinates)
- Date of the ceremony (MM/DD/YYYY format)
- Time of the ceremony (HH:MM format)

The API returns:
- Samvatsara (60-year Hindu calendar year)
- Ayana (solar half-year: Uttarayana or Dakshinayana)
- Ruthu (season: Vasantha, Grishma, Varsha, Sharada, Hemanta, Shishira)
- Masam (Hindu month: Chaitra, Vaishaka, Jyeshta, etc.)
- Paksham (lunar phase: Shukla or Krishna)
- Tithi (lunar day: 15 tithis in each paksha)
- Vaaram (day of the week with Hindu names)
- Nakshatram (27 lunar mansions)
- Sunrise and Sunset times for the location

ARCHITECTURE
=============

The API is built on:
- **Framework**: Spring Boot 3.2.3
- **Language**: Java 21
- **Build Tool**: Maven 3.8+
- **Server**: Embedded Tomcat
- **Port**: 8081

KEY COMPONENTS
==============

1. **Controllers**
   - SankalpamController: Handles REST endpoints

2. **Services**
   - SankalpamService: Business logic for Sankalpam calculation
   - GoogleGeoLocationService: Fetches coordinates from city name
   - GoogleTimeZoneService: Fetches timezone from coordinates
   - SankalpamApiClient: Calls external Sankalpam API

3. **Models**
   - SankalpamFinder: Main response POJO
   - Coordinates: Latitude/Longitude data

4. **Lookup System**
   - MappingService: Maps API values to display-friendly names
   - JsonMappingLoader: Loads JSON lookup files at startup
   - Lookup directory: Contains JSON files with mapping data

5. **Exception Handling**
   - GlobalExceptionHandler: Centralized error handling
   - Validation: Input validation on all requests

API ENDPOINTS
=============

POST /api/find
--------------
Calculates Sankalpam for given parameters

Request Body:
{
  "fullName": "John Doe",
  "city": "New York",
  "date": "2026-02-24",
  "time": "18:30"
}

Response:
{
  "success": true,
  "message": "Sankalpam found successfully",
  "data": {
    "samvatsaram": "viSvAvasu",
    "ayanam": "uttarAyaNE",
    "ruthuvu": "Shishira Ruthu",
    "maasam": "Maagham",
    "paksham": "Shukla Paksham",
    "tithi": "ashtamyAm",
    "vaaram": "bhouma",
    "nakshatram": "rOhinI",
    "sunrise": "06:41:27",
    "sunset": "17:36:52"
  },
  "timestamp": "2026-02-27T12:00:00Z"
}

GET /actuator/health
---------------------
Health check endpoint

Response:
{
  "status": "UP"
}

EXTERNAL DEPENDENCIES
=====================

1. **Google Geocoding API**
   - Purpose: Convert city name to latitude/longitude coordinates
   - Configuration: application.yml (google.api.key)
   - Rate Limit: Free tier allows limited requests

2. **Google Time Zone API**
   - Purpose: Get timezone offset for a location
   - Configuration: application.yml (google.api.key)
   - Rate Limit: Free tier allows limited requests

3. **Sankalpam Calculator API**
   - URL: https://samekadasi-324123.uc.r.appspot.com/rpc
   - Purpose: External service that calculates Panchang details
   - Parameters: City, coordinates, timezone, date, time
   - Returns: HTML formatted response with Panchang information

DATA MAPPING & LOOKUP
=====================

The API uses a JSON-based lookup system for mapping internal values to user-friendly display names:

Location: src/main/resources/lookup/

Current Mappings:
- Maasam.json: Maps Hindu month codes to display names
  Example: "kumbha" → "Maagham"

The JsonMappingLoader:
- Loads all JSON files from the lookup directory at startup
- Caches mappings in memory for fast access
- Automatically discovers new mapping files added to the directory

To add new mappings:
1. Create a new JSON file in src/main/resources/lookup/
2. Format: { "key": "displayValue" }
3. Restart the application
4. New mappings will be automatically loaded

CONFIGURATION
=============

File: src/main/resources/application.yml

Key Settings:
- google.api.key: Your Google API key (required)
- server.port: Application port (default: 8081)
- logging.level: Log level for different packages

SECURITY NOTES
==============

- The API uses Spring Security with auto-generated passwords in development
- Configure proper authentication for production
- API key should be stored securely (environment variables, not in code)
- Input validation on all endpoints
- CORS disabled by default for security

ERROR HANDLING
==============

The API returns structured error responses:

400 Bad Request - Invalid input
{
  "success": false,
  "message": "Validation failed: ...",
  "timestamp": "..."
}

500 Internal Server Error - Processing failed
{
  "success": false,
  "message": "Unexpected error: ...",
  "timestamp": "..."
}

LOGGING
=======

Logs are written to: logs/application-runtime.log

Log Levels:
- INFO: General application flow
- WARN: Potential issues
- ERROR: Errors that don't stop the app
- DEBUG: Detailed diagnostic information

The log viewer window (when run in debug mode) color-codes logs:
- Green: INFO messages
- Yellow: WARNINGS
- Red: ERRORS
- Cyan: HTTP requests

TESTING
=======

Sample Curl Commands:

Basic Test:
curl -X POST http://localhost:8081/api/find \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","city":"New York","date":"2026-02-24","time":"18:30"}'

Health Check:
curl http://localhost:8081/actuator/health

DEPLOYMENT
==========

The application is packaged as a single executable JAR file:
- Location: target/sankalpam-api-1.0.0.jar
- Execution: java -jar sankalpam-api-1.0.0.jar
- Port: Configurable (default 8081)

FOR DEVELOPERS
==============

Key Technologies:
- Spring Boot 3.2.3
- Spring Security
- Jackson (JSON processing)
- Lombok (code generation)
- Maven (build)
- JUnit 5 (testing)

Project Structure:
src/
├── main/
│   ├── java/com/sankalpam/
│   │   ├── controller/      (REST endpoints)
│   │   ├── service/         (Business logic)
│   │   ├── model/           (Data classes)
│   │   ├── exception/       (Error handling)
│   │   └── util/            (Utilities)
│   └── resources/
│       ├── lookup/          (JSON mappings)
│       └── application.yml  (Configuration)
└── test/                    (Unit tests)

Building:
mvn clean install -DskipTests

Running:
java -jar target/sankalpam-api-1.0.0.jar

FUTURE ENHANCEMENTS
===================

Planned improvements:
- Database integration for caching results
- Enhanced error messages with suggestions
- Support for multiple languages
- API rate limiting
- User authentication and authorization
- REST API documentation (Swagger/OpenAPI)
- Performance optimization
- Batch processing for multiple requests

SUPPORT & CONTACT
=================

For issues or questions:
- Check logs/application-runtime.log
- Review error responses for diagnostic information
- Ensure Google API keys are configured correctly
- Verify network connectivity to external APIs

