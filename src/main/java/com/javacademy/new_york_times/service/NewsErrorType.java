package com.javacademy.new_york_times.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NewsErrorType {
    NO_NEWS_WITH_NUMBER("Нет новости с таким номером"),
    NO_NEWS_WITH_PAGE_NUMBER("Нет страницы новости с таким номером"),
    NO_NEWS_WITH_NEXT("Нет новости с таким номером либо текст новости отсутствует"),
    NO_NEWS_WITH_AUTHOR("Нет новости с таким номером либо автор новости отсутствует");

    private final String message;
}
