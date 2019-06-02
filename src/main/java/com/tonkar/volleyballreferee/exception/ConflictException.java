package com.tonkar.volleyballreferee.exception;

import java.util.List;

public class ConflictException extends Exception {

    public ConflictException(String message) {
        super(message);
        setStackTrace(
                List.of(getStackTrace())
                        .stream()
                        .filter(stackTraceElement -> stackTraceElement.toString().startsWith("com.tonkar"))
                        .limit(10)
                        .toArray(StackTraceElement[]::new)
        );
    }

}
