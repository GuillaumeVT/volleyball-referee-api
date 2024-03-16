package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.ErrorResponse;
import io.undertow.util.BadRequestException;
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
                        BadRequestException.class,
                        HttpMessageNotReadableException.class,
                        MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ErrorResponse> badRequestExceptionHandler(Exception e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> exceptionHandler(ResponseStatusException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), e.getStatusCode());
    }
}
