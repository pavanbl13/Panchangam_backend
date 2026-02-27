package com.sankalpam.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Unified API response envelope.
 * Uses a hand-written constructor approach to avoid Lombok @Builder/@AllArgsConstructor
 * conflicts with generic static factory methods.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Map<String, List<String>> errors;
    private final Instant timestamp;

    public ApiResponse(boolean success, String message, T data, Map<String, List<String>> errors) {
        this.success   = success;
        this.message   = message;
        this.data      = data;
        this.errors    = errors;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, List<String>> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
