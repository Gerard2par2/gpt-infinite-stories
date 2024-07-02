package com.simard.infinitestories;

import com.simard.infinitestories.exceptions.InvalidCompletionException;
import com.simard.infinitestories.exceptions.MyApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MyApiException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(MyApiException e) {
        return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(InvalidCompletionException.class)
    public ResponseEntity<String> handleInvalidCompletionException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Received invalid completion from openAI");
    }
}
