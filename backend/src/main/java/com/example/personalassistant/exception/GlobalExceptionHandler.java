package com.example.personalassistant.exception;

import com.example.personalassistant.dto.ErrorDetails;
import com.example.personalassistant.dto.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Response response = new Response();
        response.setSuccess(false);
        String msg = "Data integrity violation. A unique constraint or foreign key failed.";
        if (ex.getMessage() != null && ex.getMessage().contains("UK_")) {
            msg = "Duplicate entry detected. Email or Mobile number already registered.";
        }
        ErrorDetails error = new ErrorDetails(HttpStatus.CONFLICT, msg);
        response.setError(error);
        response.setMessage(msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> handleBadCredentials(BadCredentialsException ex) {
        Response response = new Response();
        response.setSuccess(false);
        ErrorDetails error = new ErrorDetails(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        response.setError(error);
        response.setMessage("Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDenied(AccessDeniedException ex) {
        Response response = new Response();
        response.setSuccess(false);
        ErrorDetails error = new ErrorDetails(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to access this resource.");
        response.setError(error);
        response.setMessage("Access Denied: Insufficient permissions.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleIllegalArgument(IllegalArgumentException ex) {
        Response response = new Response();
        response.setSuccess(false);
        ErrorDetails error = new ErrorDetails(HttpStatus.BAD_REQUEST, ex.getMessage());
        response.setError(error);
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGenericException(Exception ex) {
        Response response = new Response();
        response.setSuccess(false);
        ErrorDetails error = new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage() != null ? ex.getMessage() : "Internal server error");
        response.setError(error);
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
