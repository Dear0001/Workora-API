package com.api.bugzapper.exception;

public class CustomNotFoundException extends RuntimeException {
    public CustomNotFoundException(String message){
        super(message);
    }
}

