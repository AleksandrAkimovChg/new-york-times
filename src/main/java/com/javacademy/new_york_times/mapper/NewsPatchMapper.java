package com.javacademy.new_york_times.mapper;

import com.javacademy.new_york_times.dto.NewsDto;
import org.springframework.stereotype.Service;

@Service
public class NewsPatchMapper {

    public NewsDto convertToNewsDtoForPatch(NewsDto oldDto, NewsDto newDto) {
        oldDto.setTitle(newDto.getTitle() != null ? newDto.getTitle() : oldDto.getTitle());
        oldDto.setText(newDto.getText() != null ? newDto.getText() : oldDto.getText());
        oldDto.setAuthor(newDto.getAuthor() != null ? newDto.getAuthor() : oldDto.getAuthor());
        return oldDto;
    }
}
