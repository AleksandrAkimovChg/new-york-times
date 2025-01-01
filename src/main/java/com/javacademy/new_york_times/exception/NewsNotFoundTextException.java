package com.javacademy.new_york_times.exception;

public class NewsNotFoundTextException extends RuntimeException {

    public NewsNotFoundTextException(String message) {
        super(message);
    }
}
