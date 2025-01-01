package com.javacademy.new_york_times.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NewsPageDto<T> {
    private List<T> content;
    private Integer countPages;
    private Integer currentPage;
    private Integer maxPageSize;
    private Integer size;
}
