package com.tonkar.volleyballreferee.exception;

public class ConflictException extends Exception {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

}
