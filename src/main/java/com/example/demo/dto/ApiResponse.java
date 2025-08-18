package com.example.demo.dto;

public class ApiResponse<T> {
    private int status;  // HTTP status code
    private String message;
    private T data;

    // Constructor thành công
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Constructor thất bại
    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}