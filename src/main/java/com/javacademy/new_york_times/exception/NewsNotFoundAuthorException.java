package com.javacademy.new_york_times.exception;

public class NewsNotFoundAuthorException extends RuntimeException  {

    public NewsNotFoundAuthorException(String message) {
        super(message);
    }
}
