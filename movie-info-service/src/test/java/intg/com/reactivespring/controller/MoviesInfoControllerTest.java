package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {

    final static String MOVIES_INFO_URL = "/v1/movieInfos";
    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp(){
        var movieinfos = List.of(new MovieInfo("abc", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo("def", "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("ghi", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos).blockLast();
    }

    @AfterEach()
    void  tearDown(){
        movieInfoRepository.deleteAll().block();
    }
    @Test
    void addMovieInfo() {
        var movieinfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.post().uri(MOVIES_INFO_URL)
                .bodyValue(movieinfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });
    }

    @Test
    void getAllMovieInfos(){
        webTestClient.get().uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfo() {


        String movieId = "abc";

        webTestClient.get().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                });


        webTestClient.get().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Batman Begins");
    }

    @Test
    void updateMovieInfo() {

        var movieinfo = new MovieInfo(null, "Batman Begins updated",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        String movieId = "abc";
        webTestClient.put().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .bodyValue(movieinfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assertEquals("Batman Begins updated", updatedMovieInfo.getName());
                });
    }


    @Test
    void updateMovieInfoNotFound() {

        var movieinfo = new MovieInfo(null, "Batman Begins updated",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        String movieId = "ter";
        webTestClient.put().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .bodyValue(movieinfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfoNotFound() {


        String movieId = "ter";
        webTestClient.get().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {
        String movieId = "abc";
        webTestClient.delete().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .isNoContent();

    }

    @Test
    void getMovieInfoByYear(){

        URI uriStr = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand().toUri();

        webTestClient.get().uri(uriStr)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

}