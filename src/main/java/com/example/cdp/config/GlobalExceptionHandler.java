package com.example.cdp.config;

import com.example.cdp.exception.EntityNotFoundException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleResourceNotFoundException(EntityNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
        .type(URI.create(EntityNotFoundException.class.getSimpleName())).build();
    return new ResponseEntity<>(error.getBody(), HttpStatus.NOT_FOUND);
  }

}
