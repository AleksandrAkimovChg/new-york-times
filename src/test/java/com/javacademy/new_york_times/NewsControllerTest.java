package com.javacademy.new_york_times;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.NewsPageDto;
import com.javacademy.new_york_times.entity.NewsEntity;
import com.javacademy.new_york_times.mapper.NewsMapper;
import com.javacademy.new_york_times.repository.NewsRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class NewsControllerTest {
    private static final int PAGE_SIZE = 10;
    private static final int MINIMUM_PAGE_LIMIT = 0;
    private static final int NUMBER_PAGE_START = 1;
    private static final int NUMBER_NEWS_IN_SCOPE_ONE = 1;
    private static final int NUMBER_NEWS_IN_SCOPE_TWO = 2;
    private static final int NUMBER_NEWS_OUT_OF_SCOPE = 2222;
    private static final String PATH_TEMPLATE_FOR_ID = "/{id}";
    private static final String PATH_TEMPLATE_FOR_NEWS_AUTHOR = "/{id}/author";
    private static final String PATH_TEMPLATE_FOR_NEWS_TEXT = "/{id}/text";
    private final RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBasePath("/news")
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();
    private final ResponseSpecification responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private NewsMapper newsMapper;

    @Test
    @DisplayName("Успешное создание новости")
    public void createSuccess() {
        int allNewsSize = newsRepository.findAll().size();
        NewsDto newsDtoExpected = NewsDto.builder()
                .title("Antifragile. Things That Gain from Disorder")
                .text("Just as human bones get stronger when subjected to stress and tension, and rumors or riots...")
                .author("Nassim Nicholas Taleb")
                .build();

        RestAssured.given(requestSpecification)
                .body(newsDtoExpected)
                .post()
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.CREATED.value());
        allNewsSize++;
        NewsDto newsDtoActual = newsMapper.toDto(newsRepository.findByNumber(allNewsSize).orElseThrow());

        assertEquals(allNewsSize, newsDtoActual.getNumber());
        assertEquals(newsDtoExpected.getTitle(), newsDtoActual.getTitle());
        assertEquals(newsDtoExpected.getText(), newsDtoActual.getText());
        assertEquals(newsDtoExpected.getAuthor(), newsDtoActual.getAuthor());
    }

    @Test
    @DisplayName("Неуспешное создание новости")
    public void createFailure() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).isPresent());
        NewsDto newsDto = NewsDto.builder()
                .number(NUMBER_NEWS_IN_SCOPE_TWO)
                .title("testTitle")
                .text("testText")
                .author("testAuthor")
                .build();
        String expected = "News with number %s - already exists".formatted(NUMBER_NEWS_IN_SCOPE_TWO);

        RestAssured.given(requestSpecification)
                .body(newsDto)
                .post()
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo(expected));
    }

    @Test
    @DisplayName("Успешное удаление новости")
    public void deleteSuccess() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).isPresent());

        Boolean actual = RestAssured.given(requestSpecification)
                .delete(PATH_TEMPLATE_FOR_ID, NUMBER_NEWS_IN_SCOPE_TWO)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(Boolean.class);

        assertTrue(actual);
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).isEmpty());
    }

    @Test
    @DisplayName("Неуспешное удаление новости")
    public void deleteFailure() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_OUT_OF_SCOPE).isEmpty());

        Boolean actual = RestAssured.given(requestSpecification)
                .delete(PATH_TEMPLATE_FOR_ID, NUMBER_NEWS_OUT_OF_SCOPE)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract()
                .body()
                .as(Boolean.class);

        assertFalse(actual);
    }

    @Test
    @DisplayName("Успешное получение новости по id")
    public void getByIdSuccess() {
        NewsDto expected = newsMapper.toDto(newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).orElseThrow());
        assertEquals(NUMBER_NEWS_IN_SCOPE_TWO, expected.getNumber());

        RestAssured.given(requestSpecification)
                .get(PATH_TEMPLATE_FOR_ID, NUMBER_NEWS_IN_SCOPE_TWO)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .body("number", equalTo(expected.getNumber()))
                .body("title", equalTo(expected.getTitle()))
                .body("text", equalTo(expected.getText()))
                .body("author", equalTo(expected.getAuthor()));
    }

    @Test
    @DisplayName("Неуспешное получение новости по id")
    public void getByIdFailure() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_OUT_OF_SCOPE).isEmpty());

        RestAssured.given(requestSpecification)
                .get(PATH_TEMPLATE_FOR_ID, NUMBER_NEWS_OUT_OF_SCOPE)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Нет новости с таким номером"));
    }

    @Test
    @DisplayName("Успешное получение новостей по странице пагинации")
    public void getAllSuccess() {
        NewsDto expected = newsMapper.toDto(newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_ONE).orElseThrow());
        assertEquals(NUMBER_NEWS_IN_SCOPE_ONE, expected.getNumber());
        int totalPages = (int) Math.ceil(newsRepository.findAll().size() * 1.00 / PAGE_SIZE);

        NewsPageDto<NewsDto> newsPageDto = RestAssured.given(requestSpecification)
                .queryParam("page", MINIMUM_PAGE_LIMIT)
                .get()
                .then()
                .spec(responseSpecification)
                .contentType(ContentType.JSON)
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(new TypeRef<>() {
                });
        NewsDto actual = newsPageDto.getContent().get(0);

        assertEquals(totalPages, newsPageDto.getCountPages());
        assertEquals(NUMBER_PAGE_START, newsPageDto.getCurrentPage());
        assertEquals(PAGE_SIZE, newsPageDto.getMaxPageSize());
        assertEquals(PAGE_SIZE, newsPageDto.getSize());

        assertEquals(expected.getNumber(), actual.getNumber());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getAuthor(), actual.getAuthor());
    }

    @Test
    @DisplayName("Успешное обновление новости")
    public void patchSuccess() {
        NewsEntity newsEntity = newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).orElseThrow();
        assertEquals(NUMBER_NEWS_IN_SCOPE_TWO, newsEntity.getNumber());
        String expectedTest = "Today is not Groundhog Day";
        NewsDto requestNewsDto = NewsDto.builder()
                .title(expectedTest)
                .build();
        String expectedResponse = "Обновлено";

        RestAssured.given(requestSpecification)
                .body(requestNewsDto)
                .patch(PATH_TEMPLATE_FOR_ID, NUMBER_NEWS_IN_SCOPE_TWO)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .body(equalTo(expectedResponse));
        NewsEntity newsEntityActual = newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).orElseThrow();

        assertEquals(newsEntity.getNumber(), newsEntityActual.getNumber());
        assertEquals(expectedTest, newsEntityActual.getTitle());
        assertEquals(newsEntity.getText(), newsEntityActual.getText());
        assertEquals(newsEntity.getAuthor(), newsEntityActual.getAuthor());
    }

    @Test
    @DisplayName("Неуспешное обновление новости")
    public void patchFailure() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_OUT_OF_SCOPE).isEmpty());
        NewsDto requestNewsDto = NewsDto.builder()
                .title("test")
                .build();

        RestAssured.given(requestSpecification)
                .body(requestNewsDto)
                .patch(PATH_TEMPLATE_FOR_ID, NUMBER_NEWS_OUT_OF_SCOPE)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Нет новости с таким номером"));
    }

    @Test
    @DisplayName("Получение текста новости")
    public void getTextNewsSuccess() {
        String expected = newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).orElseThrow().getText();

        RestAssured.given(requestSpecification)
                .get(PATH_TEMPLATE_FOR_NEWS_TEXT, NUMBER_NEWS_IN_SCOPE_TWO)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .body(equalTo(expected));
    }

    @Test
    @DisplayName("Неуспешное получение текста новости")
    public void getTextNewsFailure() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_OUT_OF_SCOPE).isEmpty());

        RestAssured.given(requestSpecification)
                .get(PATH_TEMPLATE_FOR_NEWS_TEXT, NUMBER_NEWS_OUT_OF_SCOPE)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Нет новости с таким номером либо текст новости отсутствует"));
    }

    @Test
    @DisplayName("Успешное получение автора новости")
    public void getAuthorNewsSuccess() {
        String expected = newsRepository.findByNumber(NUMBER_NEWS_IN_SCOPE_TWO).orElseThrow().getAuthor();

        RestAssured.given(requestSpecification)
                .get(PATH_TEMPLATE_FOR_NEWS_AUTHOR, NUMBER_NEWS_IN_SCOPE_TWO)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.OK.value())
                .body(equalTo(expected));
    }

    @Test
    @DisplayName("Неуспешное получение автора новости")
    public void getAuthorNewsFailure() {
        assertTrue(newsRepository.findByNumber(NUMBER_NEWS_OUT_OF_SCOPE).isEmpty());

        RestAssured.given(requestSpecification)
                .get(PATH_TEMPLATE_FOR_NEWS_AUTHOR, NUMBER_NEWS_OUT_OF_SCOPE)
                .then()
                .spec(responseSpecification)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("Нет новости с таким номером либо автор новости отсутствует"));
    }
}
