package com.example.personalassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response {
    private Object data;
    private ErrorDetails error;
    private String message;
    private Boolean success;

    public Response(Object data, ErrorDetails error) {
        this.data = data;
        this.error = error;
    }

    public Response(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(Object data, ErrorDetails error, String message, Boolean success) {
        this.data = data;
        this.error = error;
        this.message = message;
        this.success = success;
    }
}
