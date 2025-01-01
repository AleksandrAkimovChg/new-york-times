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


@Service
@RequiredArgsConstructor
public class NewsService {
    private static final int PAGE_SIZE = 10;
    private static final int COUNT_PAGE_NUMBER_WITHOUT_ZERO = 1;
    private static final String NO_NEWS_WITH_NUMBER = "Нет новости с таким номером";
    private static final String NO_NEWS_WITH_PAGE_NUMBER = "Нет страницы новости с таким номером";
    private static final String NO_NEWS_WITH_NEXT = "Нет новости с таким номером либо текст новости отсутствует";
    private static final String NO_NEWS_WITH_AUTHOR = "Нет новости с таким номером либо автор новости отсутствует";
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    private final NewsPatchMapper newsPatchMapper;

    public void save(NewsDto dto) {
        newsRepository.save(newsMapper.toEntity(dto));
    }


    public List<NewsDto> findAll() {
        return newsMapper.toDtos(newsRepository.findAll());
    }

    public NewsPageDto<NewsDto> findAll(int pageNumber) {
        List<NewsDto> newsDtoList = findAll();
        int totalPages = (int) Math.ceil(newsDtoList.size() * 1.00 / PAGE_SIZE);
        if (totalPages < pageNumber) {
            throw new NewsNotFoundException(NO_NEWS_WITH_PAGE_NUMBER);
        }
        List<NewsDto> newsDtoListByPage = newsDtoList.stream()
                .sorted(Comparator.comparing(NewsDto::getNumber))
                .skip(PAGE_SIZE * pageNumber).limit(PAGE_SIZE).toList();

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

    public void update(NewsDto dto) {
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
