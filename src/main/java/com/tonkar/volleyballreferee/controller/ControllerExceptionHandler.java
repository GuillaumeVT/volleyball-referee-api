package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.exception.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    ResponseEntity<Reason> exceptionHandler(ValidationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Reason> exceptionHandler(ConstraintViolationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<Reason> exceptionHandler(BadRequestException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<Reason> exceptionHandler(ConflictException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<Reason> exceptionHandler(ForbiddenException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Reason> exceptionHandler(NotFoundException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<Reason> exceptionHandler(UnauthorizedException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new Reason(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @NoArgsConstructor @AllArgsConstructor @Getter @Setter
    public static class Reason {
        private String reason;
    }
}
