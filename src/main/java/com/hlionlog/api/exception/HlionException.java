package com.hlionlog.api.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class HlionException extends RuntimeException {

    public Map<String, String> validation = new HashMap<>();

    public HlionException(String message) {
        super(message);
    }

    public HlionException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract int getStatusCode();

    public void addValidation(String fieldName, String message) {
        this.validation.put(fieldName, message);
    }
}
