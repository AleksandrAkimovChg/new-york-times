package com.javacademy.new_york_times.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDto {
    private Integer number;
    private String title;
    private String text;
    private String author;
}
