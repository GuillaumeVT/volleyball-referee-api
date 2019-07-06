package com.tonkar.volleyballreferee.exception;

import java.util.List;

public class UnauthorizedException extends Exception {

    public UnauthorizedException(String message) {
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
