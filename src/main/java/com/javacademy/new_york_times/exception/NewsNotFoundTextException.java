package com.javacademy.new_york_times.exception;

import com.javacademy.new_york_times.service.NewsErrorType;

public class NewsNotFoundTextException extends RuntimeException {

    public NewsNotFoundTextException(NewsErrorType message) {
        super(message.getMessage());
    }
}
