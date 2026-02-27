package com.sankalpam.service.impl;

import com.sankalpam.model.Coordinates;
import com.sankalpam.model.SankalpamFinder;
import com.sankalpam.service.SankalpamApiClient;
import com.sankalpam.service.mapping.MappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SankalpamApiClientImpl implements SankalpamApiClient {

    @Autowired
    private MappingService mappingService;

    @Override
    public SankalpamFinder fetchSankalpam(String city, Coordinates coords, String timezone, String dateStr, String timeStr) {
        try {
            String apiDate = LocalDate.parse(dateStr).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            String apiTime = normalizeApiTime(timeStr);

            String url = String.format(
                    "https://samekadasi-324123.uc.r.appspot.com/rpc?action=findSankalpam&cityfld=%s&latfld=%f&lngfld=%f&tzfld=%s&sankalpamdatestr=%s&sankalpamtimestr=%s",
                    URLEncoder.encode(city, StandardCharsets.UTF_8),
                    coords.lat(), coords.lng(),
                    URLEncoder.encode(timezone, StandardCharsets.UTF_8),
                    URLEncoder.encode(apiDate, StandardCharsets.UTF_8),
                    URLEncoder.encode(apiTime, StandardCharsets.UTF_8)
            );

            log.info("Calling Sankalpam API: city={}, date={}, time={}, timezone={}", city, dateStr, timeStr, timezone);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(15))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            log.info("Sankalpam API response status: {}", response.statusCode());

            // Print the complete API response for debugging
            String responseBody = response.body();
            log.info("");
            log.info("================================================================================");
            log.info("SANKALPAM API RESPONSE");
            log.info("================================================================================");
            log.info("URL: {}", url.replace(URLEncoder.encode(timezone, StandardCharsets.UTF_8), "[TIMEZONE]"));
            log.info("Status Code: {}", response.statusCode());
            log.info("--------------------------------------------------------------------------------");
            log.info("Response Body:");
            log.info("{}", responseBody);
            log.info("================================================================================");
            log.info("");

            if (response.statusCode() != 200) {
                log.warn("Sankalpam API returned non-200 status: {}. Using fallback data.", response.statusCode());
                return createFallbackSankalpam(city, dateStr, timeStr);
            }

            SankalpamFinder finder = new SankalpamFinder();
            finder.setDate(dateStr);
            finder.setTime(timeStr);
            finder.setCity(city);

            if (!applyHtmlResponse(responseBody, finder)) {
                log.warn("Failed to parse HTML response. Using fallback data.");
                return createFallbackSankalpam(city, dateStr, timeStr);
            }

            // Log parsed Panchanga details
            log.info("");
            log.info("================================================================================");
            log.info("PARSED PANCHANGA DETAILS");
            log.info("================================================================================");
            log.info("Samvatsaram:  {}", finder.getSamvatsaram());
            log.info("Ayanam:       {}", finder.getAyanam());
            log.info("Ruthuvu:      {}", finder.getRuthu());
            log.info("Maasam:       {}", finder.getMasam());
            log.info("Paksham:      {}", finder.getPaksham());
            log.info("Tithi:        {}", finder.getTithi());
            log.info("Vaaram:       {}", finder.getVaasaram());
            log.info("Nakshatram:   {}", finder.getNakshatram());
            log.info("Sunrise:      {}", finder.getSunrise());
            log.info("Sunset:       {}", finder.getSunset());
            log.info("Valid Until:  {}", finder.getValidUntil());
            log.info("================================================================================");
            log.info("");

            log.info("Successfully fetched Sankalpam data from external API");
            return finder;

        } catch (Exception e) {
            log.error("Failed to fetch Sankalpam data from external API. Using fallback data.", e);
            return createFallbackSankalpam(city, dateStr, timeStr);
        }
    }

    private boolean applyHtmlResponse(String responseBody, SankalpamFinder finder) {
        try {
            List<String> boldValues = extractBoldValues(responseBody);
            if (boldValues.size() < 8) {
                log.warn("Expected at least 8 bold values, but got: {}", boldValues.size());
                return false;
            }

            finder.setSamvatsaram(boldValues.get(0));
            finder.setAyanam(mappingService.mapAyanam(boldValues.get(1)));

            // Extract Maasam from date range instead of using API response value
            String maasam = extractMaasamFromDateRange(finder.getDate());
            finder.setMasam(maasam);

            // Extract Ruthuvu based on Maasam instead of using API response value
            String ruthuvu = extractRuthuFromMaasam(maasam);
            finder.setRuthu(ruthuvu);

            finder.setPaksham(mappingService.mapPaksham(boldValues.get(4)));
            finder.setTithi(mappingService.mapTithi(boldValues.get(5)));

            // Extract Vaaram based on day of week instead of using API response value
            String vaaram = extractVaaramFromDate(finder.getDate());
            finder.setVaasaram(vaaram);

            finder.setNakshatram(mappingService.mapNakshatram(boldValues.get(7)));

            finder.setSunrise(extractTagValue(responseBody, "Sunrise"));
            finder.setSunset(extractTagValue(responseBody, "Sunset"));
            finder.setValidUntil(extractValidThroughTime(responseBody, finder.getDate(), finder.getTime()));

            return finder.getSunrise() != null && finder.getSunset() != null;
        } catch (Exception e) {
            log.error("Exception occurred while applying HTML response parsing", e);
            return false;
        }
    }

    private List<String> extractBoldValues(String responseBody) {
        List<String> values = new ArrayList<>();
        Matcher matcher = Pattern.compile("<b>(.*?)</b>", Pattern.CASE_INSENSITIVE).matcher(responseBody);
        while (matcher.find()) {
            values.add(matcher.group(1).trim());
        }
        return values;
    }

    private String extractTagValue(String responseBody, String label) {
        try {
            Pattern pattern = Pattern.compile(label + ":\\s*<i>([^<]+)</i>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(responseBody);
            return matcher.find() ? matcher.group(1).trim() : null;
        } catch (Exception e) {
            log.error("Exception occurred while extracting tag value for label: {}", label, e);
            return null;
        }
    }

    private String normalizeApiTime(String timeStr) {
        try {
            String trimmed = timeStr == null ? "" : timeStr.trim();
            DateTimeFormatter apiOut = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

            if (trimmed.toLowerCase(Locale.US).contains("am") || trimmed.toLowerCase(Locale.US).contains("pm")) {
                DateTimeFormatter ampmIn = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("h:mm a")
                        .toFormatter(Locale.US);
                return LocalTime.parse(trimmed, ampmIn).format(apiOut);
            }

            DateTimeFormatter twentyFourIn = DateTimeFormatter.ofPattern("H:mm");
            return LocalTime.parse(trimmed, twentyFourIn).format(apiOut);
        } catch (Exception e) {
            log.error("Exception occurred while normalizing API time: {}", timeStr, e);
            return timeStr;
        }
    }

    private String extractValidThroughTime(String responseBody, String date, String time) {
        try {
            // The API response format is: "valid through {TIME_INFO}: <br/>"
            // Examples:
            //   - "valid through 05:30:12 AM of following day: <br/>"
            //   - "valid through 06:19:49 AM: <br/>"
            // We need to capture everything between "valid through" and the final ": <br"
            Pattern pattern = Pattern.compile("valid through\\s+(.+?)\\s*:\\s*<br", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                String validThroughTime = matcher.group(1).trim();
                log.info("Extracted valid through time: {}", validThroughTime);
                return validThroughTime;
            }

            log.warn("Could not extract valid through time from response");
            log.debug("Response body snippet (first 500 chars): {}", responseBody.substring(0, Math.min(500, responseBody.length())));
            return null;
        } catch (Exception e) {
            log.error("Exception occurred while extracting valid through time", e);
            return null;
        }
    }

    private SankalpamFinder createFallbackSankalpam(String city, String dateStr, String timeStr) {
        log.info("Creating fallback Sankalpam data for: city={}, date={}, time={}", city, dateStr, timeStr);

        SankalpamFinder finder = new SankalpamFinder();
        finder.setDate(dateStr);
        finder.setTime(timeStr);
        finder.setCity(city);

        // Mock Panchanga data based on current date (Feb 24, 2026)
        finder.setSamvatsaram("Pingala");
        finder.setAyanam("Uttarayanam");
        finder.setRuthu("Vasantha Ruthu");
        finder.setMasam("Phalguna");
        finder.setPaksham("Krishna Paksham");
        finder.setTithi("Ashtami");
        finder.setVaasaram("Soma Vaasaram (Monday)");
        finder.setNakshatram("Purva Phalguni");
        finder.setRasi("Simha (Leo)");

        // Default sunrise/sunset
        finder.setSunrise("06:18");
        finder.setSunset("18:35");
        finder.setValidUntil(dateStr + "T23:59:59");

        return finder;
    }

    /**
     * Extract Maasam based on the date and date ranges defined in Maasam.json
     * Supports both YYYY-MM-DD and MM/DD/YYYY date formats
     *
     * Each Maasam has a date range like "15/02-14/03" (Phalgunamu)
     * If the date falls within that range, return that Maasam name
     */
    String extractMaasamFromDateRange(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                log.warn("Date string is empty, cannot extract Maasam");
                return null;
            }

            int month = 0;
            int day = 0;

            // Handle both YYYY-MM-DD and MM/DD/YYYY formats
            if (dateStr.contains("-")) {
                // Format: YYYY-MM-DD
                String[] dateParts = dateStr.split("-");
                if (dateParts.length < 3) {
                    log.warn("Invalid date format: {}", dateStr);
                    return null;
                }
                month = Integer.parseInt(dateParts[1]);
                day = Integer.parseInt(dateParts[2]);
            } else if (dateStr.contains("/")) {
                // Format: MM/DD/YYYY
                String[] dateParts = dateStr.split("/");
                if (dateParts.length < 2) {
                    log.warn("Invalid date format: {}", dateStr);
                    return null;
                }
                month = Integer.parseInt(dateParts[0]);
                day = Integer.parseInt(dateParts[1]);
            } else {
                log.warn("Unsupported date format: {}", dateStr);
                return null;
            }

            log.debug("Extracting Maasam for date: month={}, day={}", month, day);

            // Get the Maasam mapping from JSON
            java.util.Map<String, java.util.Map<String, Object>> maasamMap =
                mappingService.getAllMaasamMappings();

            // Check each Maasam's date range
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : maasamMap.entrySet()) {
                String maasamName = entry.getKey();
                java.util.Map<String, Object> maasamData = entry.getValue();

                @SuppressWarnings("unchecked")
                java.util.List<Integer> months = (java.util.List<Integer>) maasamData.get("months");
                String range = (String) maasamData.get("range");

                if (months == null || months.isEmpty()) {
                    continue;
                }

                // Check if date falls in the Maasam range
                if (isDateInMaasamRange(month, day, range, months)) {
                    log.info("Matched Maasam: {} for date: {}, range: {}", maasamName, dateStr, range);
                    return maasamName;
                }
            }

            log.warn("No matching Maasam found for date: {}", dateStr);
            return null;

        } catch (Exception e) {
            log.error("Exception occurred while extracting Maasam from date range: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Check if the given date falls within the Maasam's date range
     * Range format: "15/02-14/03" means 15th Feb to 14th Mar
     *
     * Special handling for ranges that cross year boundary like "15/12-14/01"
     * (December 15 to January 14 of next year)
     */
    boolean isDateInMaasamRange(int month, int day, String range, java.util.List<Integer> monthsList) {
        try {
            // Parse range like "15/02-14/03"
            String[] parts = range.split("-");
            if (parts.length != 2) {
                return false;
            }

            String[] startParts = parts[0].split("/");
            String[] endParts = parts[1].split("/");

            if (startParts.length < 2 || endParts.length < 2) {
                return false;
            }

            int startDay = Integer.parseInt(startParts[0]);
            int startMonth = Integer.parseInt(startParts[1]);
            int endDay = Integer.parseInt(endParts[0]);
            int endMonth = Integer.parseInt(endParts[1]);

            log.debug("Checking date {}/{} against range {}/{} to {}/{}",
                month, day, startMonth, startDay, endMonth, endDay);

            // Check if date falls within the range
            if (startMonth <= endMonth) {
                // Normal range (e.g., 02/15 - 03/14 for Phalgunamu)
                if (month > startMonth && month < endMonth) {
                    return true;
                }
                if (month == startMonth && day >= startDay) {
                    return true;
                }
                if (month == endMonth && day <= endDay) {
                    return true;
                }
            } else {
                // Range crosses year boundary (e.g., 12/15 - 01/14 for Pushyamu)
                if (month > startMonth || month < endMonth) {
                    return true;
                }
                if (month == startMonth && day >= startDay) {
                    return true;
                }
                if (month == endMonth && day <= endDay) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Exception occurred while checking date in Maasam range", e);
            return false;
        }
    }

    /**
     * Extract Ruthuvu based on Maasam value using Ruthuvu.json mapping
     * Each Ruthuvu spans 2 Maasams, so we look up which Ruthuvu contains the given Maasam
     *
     * Example: If Maasam is "Phalgunamu", it's in Ruthuvu.Shishira (which has ["Maghamu", "Phalgunamu"])
     */
    String extractRuthuFromMaasam(String maasam) {
        try {
            if (maasam == null || maasam.isEmpty()) {
                log.warn("Maasam is empty, cannot extract Ruthuvu");
                return null;
            }

            log.debug("Extracting Ruthuvu for Maasam: {}", maasam);

            // Get the Ruthuvu mapping from JSON
            java.util.Map<String, java.util.Map<String, Object>> ruthuMap =
                mappingService.getAllRuthuMappings();

            if (ruthuMap == null || ruthuMap.isEmpty()) {
                log.warn("Ruthuvu mapping is not available");
                return null;
            }

            // Check each Ruthuvu's maasam list
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : ruthuMap.entrySet()) {
                String ruthuName = entry.getKey();
                java.util.Map<String, Object> ruthuData = entry.getValue();

                @SuppressWarnings("unchecked")
                java.util.List<String> maasams = (java.util.List<String>) ruthuData.get("maasam");

                if (maasams == null || maasams.isEmpty()) {
                    continue;
                }

                // Check if the given Maasam is in this Ruthuvu's list
                for (String m : maasams) {
                    if (m != null && m.equalsIgnoreCase(maasam)) {
                        log.info("Matched Ruthuvu: {} for Maasam: {}", ruthuName, maasam);
                        return ruthuName;
                    }
                }
            }

            log.warn("No matching Ruthuvu found for Maasam: {}", maasam);
            return null;

        } catch (Exception e) {
            log.error("Exception occurred while extracting Ruthuvu from Maasam: {}", maasam, e);
            return null;
        }
    }

    /**
     * Extract Vaaram (day of week) based on the date using Vaasare.json mapping
     *
     * Vaasare maps days of week to their Hindu names:
     * Sunday -> Bhanu, Monday -> Indu, Tuesday -> Bhowma, etc.
     */
    String extractVaaramFromDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                log.warn("Date string is empty, cannot extract Vaaram");
                return null;
            }

            log.info("Extracting Vaaram for date: {}", dateStr);

            // Parse the date to get day of week
            java.time.LocalDate date = parseDate(dateStr);
            if (date == null) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }

            // Get day of week name
            String dayOfWeek = date.getDayOfWeek().toString();
            log.info("Extracted day of week: {} for date: {}", dayOfWeek, dateStr);

            // Look up Vaasare mapping
            String vaasare = mappingService.mapVaasare(dayOfWeek);

            if (vaasare != null) {
                log.info("Matched Vaasare: {} for day: {} ({} date)", vaasare, dayOfWeek, dateStr);
                return vaasare;
            }

            log.warn("No matching Vaasare found for day: {}", dayOfWeek);
            return null;

        } catch (Exception e) {
            log.error("Exception occurred while extracting Vaaram from date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Parse date string in both YYYY-MM-DD and MM/DD/YYYY formats
     */
    java.time.LocalDate parseDate(String dateStr) {
        try {
            if (dateStr.contains("-")) {
                // Format: YYYY-MM-DD
                return java.time.LocalDate.parse(dateStr);
            } else if (dateStr.contains("/")) {
                // Format: MM/DD/YYYY
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
                return java.time.LocalDate.parse(dateStr, formatter);
            }
            return null;
        } catch (Exception e) {
            log.error("Error parsing date: {}", dateStr, e);
            return null;
        }
    }
}
