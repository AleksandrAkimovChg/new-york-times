package com.javacademy.new_york_times.exception;

import com.javacademy.new_york_times.service.NewsErrorType;

public class NewsNotFoundException extends RuntimeException {

    public NewsNotFoundException(NewsErrorType message) {
        super(message.getMessage());
    }
}
