package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.ErrorResponseDto;
import jakarta.validation.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ExceptionHandler({ ValidationException.class,
                        ConstraintViolationException.class,
                        MissingServletRequestParameterException.class,
                        MethodArgumentNotValidException.class,
                        HttpMessageNotReadableException.class,
                        MethodArgumentTypeMismatchException.class,
                        IllegalArgumentException.class
    })
    ResponseEntity<ErrorResponseDto> badRequestExceptionHandler(Exception e) {
        return new ResponseEntity<>(new ErrorResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponseDto> exceptionHandler(ResponseStatusException e) {
        return new ResponseEntity<>(new ErrorResponseDto(e.getMessage()), e.getStatusCode());
    }
}
