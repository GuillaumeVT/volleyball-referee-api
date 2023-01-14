package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ErrorResponse> exceptionHandler(ValidationException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> exceptionHandler(ConstraintViolationException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> exceptionHandler(MissingServletRequestParameterException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> exceptionHandler(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> exceptionHandler(ResponseStatusException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), e.getStatusCode());
    }
}
