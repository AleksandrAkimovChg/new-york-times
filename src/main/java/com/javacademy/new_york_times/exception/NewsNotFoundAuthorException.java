package com.javacademy.new_york_times.exception;

import com.javacademy.new_york_times.service.NewsErrorType;

public class NewsNotFoundAuthorException extends RuntimeException  {

    public NewsNotFoundAuthorException(NewsErrorType message) {
        super(message.getMessage());
    }
}
