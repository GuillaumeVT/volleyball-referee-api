package com.tonkar.volleyballreferee.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    ResponseEntity<BadRequestReason> exceptionHandler(ValidationException e) {
        return new ResponseEntity<>(new BadRequestReason(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<BadRequestReason> exceptionHandler(ConstraintViolationException e) {
        return new ResponseEntity<>(new BadRequestReason(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @NoArgsConstructor @AllArgsConstructor @Getter @Setter
    public static class BadRequestReason {

        private String badRequestReason;

    }
}
