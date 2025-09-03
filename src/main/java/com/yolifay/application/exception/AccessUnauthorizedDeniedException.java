package com.yolifay.application.exception;

public class AccessUnauthorizedDeniedException extends RuntimeException {
    public AccessUnauthorizedDeniedException(String message) {
        super(message);
    }
}
