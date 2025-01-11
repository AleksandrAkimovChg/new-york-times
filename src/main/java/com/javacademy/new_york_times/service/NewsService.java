package com.javacademy.new_york_times.service;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.NewsPageDto;
import com.javacademy.new_york_times.entity.NewsEntity;
import com.javacademy.new_york_times.exception.NewsNotFoundAuthorException;
import com.javacademy.new_york_times.exception.NewsNotFoundException;
import com.javacademy.new_york_times.exception.NewsNotFoundTextException;
import com.javacademy.new_york_times.mapper.NewsMapper;
import com.javacademy.new_york_times.mapper.NewsPatchMapper;
import com.javacademy.new_york_times.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static com.javacademy.new_york_times.service.NewsErrorType.NO_NEWS_WITH_AUTHOR;
import static com.javacademy.new_york_times.service.NewsErrorType.NO_NEWS_WITH_NEXT;
import static com.javacademy.new_york_times.service.NewsErrorType.NO_NEWS_WITH_NUMBER;
import static com.javacademy.new_york_times.service.NewsErrorType.NO_NEWS_WITH_PAGE_NUMBER;


@Service
@RequiredArgsConstructor
public class NewsService {
    private static final int PAGE_SIZE = 10;
    private static final int COUNT_PAGE_NUMBER_WITHOUT_ZERO = 1;

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    private final NewsPatchMapper newsPatchMapper;

    public void save(NewsDto dto) {
        newsRepository.save(newsMapper.toEntity(dto));
    }

    public NewsPageDto<NewsDto> findAll(int pageNumber) {
        List<NewsEntity> newsEntityList = newsRepository.findAll();
        int totalPages = (int) Math.ceil(newsEntityList.size() * 1.00 / PAGE_SIZE);
        if (totalPages < pageNumber) {
            throw new NewsNotFoundException(NO_NEWS_WITH_PAGE_NUMBER);
        }
        List<NewsDto> newsDtoListByPage = newsMapper.toDtos(newsEntityList.stream()
                .sorted(Comparator.comparing(NewsEntity::getNumber))
                .skip(PAGE_SIZE * pageNumber).limit(PAGE_SIZE).toList());

        return new NewsPageDto<>(newsDtoListByPage, totalPages,
                pageNumber + COUNT_PAGE_NUMBER_WITHOUT_ZERO, PAGE_SIZE, newsDtoListByPage.size());
    }

    public NewsDto findByNumber(Integer number) {
        return newsMapper.toDto(newsRepository.findByNumber(number)
                .orElseThrow(() -> new NewsNotFoundException(NO_NEWS_WITH_NUMBER)));
    }

    public boolean deleteByNumber(Integer number) {
        return newsRepository.deleteByNumber(number);
    }

    private void update(NewsDto dto) {
        newsRepository.update(newsMapper.toEntity(dto));
    }

    public void updateForPatch(NewsDto oldDto, NewsDto newDto) {
        NewsDto newsDto = newsPatchMapper.convertToNewsDtoForPatch(oldDto, newDto);
        this.update(newsDto);
    }

    public String getNewsText(Integer newsNumber) {
        return newsRepository.findByNumber(newsNumber).map(NewsEntity::getText)
                .orElseThrow(() -> new NewsNotFoundTextException(NO_NEWS_WITH_NEXT));
    }

    public String getNewsAuthor(Integer newsNumber) {
        return newsRepository.findByNumber(newsNumber).map(NewsEntity::getAuthor)
                .orElseThrow(() -> new NewsNotFoundAuthorException(NO_NEWS_WITH_AUTHOR));
    }
}
