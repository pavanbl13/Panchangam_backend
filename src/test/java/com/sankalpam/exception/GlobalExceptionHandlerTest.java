package com.sankalpam.exception;

import com.sankalpam.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpInputMessage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleValidation returns 422 with field errors")
    void handleValidation_ReturnsUnprocessableEntity() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "date", "Date is required"));
        bindingResult.addError(new FieldError("request", "city", "City is required"));

        // Use a real MethodParameter from a known method
        java.lang.reflect.Method method = String.class.getMethod("toString");
        org.springframework.core.MethodParameter param =
                new org.springframework.core.MethodParameter(method, -1);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("handleMalformedJson returns 400")
    void handleMalformedJson_ReturnsBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error", (HttpInputMessage) null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleMalformedJson(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("handleTypeMismatch returns 400")
    void handleTypeMismatch_ReturnsBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "bad", Integer.class, "id", null, new NumberFormatException("bad"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("id"));
    }

    @Test
    @DisplayName("handleNotFound returns 404")
    void handleNotFound_ReturnsNotFound() {
        NoResourceFoundException ex = new NoResourceFoundException(
                org.springframework.http.HttpMethod.GET, "api/missing");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("handleMethodNotAllowed returns 405")
    void handleMethodNotAllowed_ReturnsMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotAllowed(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("DELETE"));
    }

    @Test
    @DisplayName("handleMethodNotAllowed with null method returns generic message")
    void handleMethodNotAllowed_NullMethod() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException((String) null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotAllowed(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    @DisplayName("handleGeneric returns 500")
    void handleGeneric_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }
}


