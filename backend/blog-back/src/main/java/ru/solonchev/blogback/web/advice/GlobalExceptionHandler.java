package ru.solonchev.blogback.web.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.solonchev.blogback.web.dto.ApiErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception) {
        log.error("Caught exception", exception);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse()
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setMessage("Unexpected error occurred");
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse()
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setMessage(exception.getMessage());
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(exception = IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException exception) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse()
                .setStatus(HttpStatus.CONFLICT.value())
                .setMessage(exception.getMessage());
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.CONFLICT);
    }
}
