package com.javacademy.new_york_times.controller;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.NewsPageDto;
import com.javacademy.new_york_times.exception.NewsNotFoundAuthorException;
import com.javacademy.new_york_times.exception.NewsNotFoundException;
import com.javacademy.new_york_times.exception.NewsNotFoundTextException;
import com.javacademy.new_york_times.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {
    private final NewsService newsService;

    @PostMapping
    @CacheEvict(value = "newsByPage", allEntries = true)
    public ResponseEntity<String> createNewsItem(@RequestBody NewsDto newsDto) {
        try {
            newsService.save(newsDto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Caching(
            evict = {
                    @CacheEvict(value = "news"),
                    @CacheEvict(value = "newsByPage", allEntries = true)
            }
    )
    public ResponseEntity<Boolean> deleteNewsById(@PathVariable Integer id) {
        if (newsService.deleteByNumber(id)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "news")
    public ResponseEntity<?> getNewsById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(newsService.findByNumber(id));
        } catch (NewsNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping
    @Cacheable(value = "newsByPage")
    public ResponseEntity<NewsPageDto<NewsDto>> getNews(@RequestParam Integer page) {
        return ResponseEntity.ok(newsService.findAll(page));
    }

    @PatchMapping("/{id}")
    @Caching(
            evict = {
                    @CacheEvict(value = "news", key = "#id"),
                    @CacheEvict(value = "newsByPage", allEntries = true)
            }
    )
    public ResponseEntity<String> patchNews(@PathVariable Integer id, @RequestBody NewsDto newDto) {
        try {
            NewsDto oldDto = newsService.findByNumber(id);
            newsService.updateForPatch(oldDto, newDto);
            return ResponseEntity.ok().body("Обновлено");
        } catch (NewsNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}/text")
    public ResponseEntity<String> getTextNewsById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(newsService.getNewsText(id));
        } catch (NewsNotFoundTextException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}/author")
    public ResponseEntity<String> getAuthorNewsById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(newsService.getNewsAuthor(id));
        } catch (NewsNotFoundAuthorException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}
