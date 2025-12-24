package com.example.ordermanager.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Unified response wrapper for API responses
 * @param <T> The type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String message;
    private T data;
    private String timestamp;

    private ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Creates a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "Success", data);
    }

    /**
     * Creates a successful response with custom message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, 200, message, data);
    }

    /**
     * Creates a successful response with custom code and data
     */
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    /**
     * Creates an error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, 400, message, null);
    }

    /**
     * Creates an error response with custom code
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * Creates an error response with custom code, message and data
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    /**
     * Creates a not found response
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, 404, message, null);
    }

    /**
     * Creates a bad request response
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(false, 400, message, null);
    }

    /**
     * Creates an internal server error response
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(false, 500, message, null);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}