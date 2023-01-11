package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    final static String MOVIES_INFO_URL = "/v1/movieInfos";
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;


    @Test
    void getAllMoviesInfo(){

        var movieinfos = List.of(new MovieInfo("abc", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo("def", "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("ghi", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));


        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient.get().uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfo(){
        String movieId = "abc";
        var _movieInfo = new MovieInfo("abc", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoServiceMock.getMovieInfo(isA(String.class)))
                .thenReturn(Mono.just(_movieInfo));



        webTestClient.get().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                });
    }


    @Test
    void addMovieInfo() {
        var movieinfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId", "Batman Begins1",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))
        ));

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
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });
    }


    @Test
    void updateMovieInfo() {

        String movieId = "abc";

        var movieinfo = new MovieInfo(null, "Batman Begins updated",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));


        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(
                new MovieInfo(movieId, "Batman Begins updated",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))
        ));

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
    void deleteMovieInfo() {
        String movieId = "abc";

        when(movieInfoServiceMock.deleteMovieInfo(isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri(MOVIES_INFO_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .isNoContent();

    }

    void addMovieInfoWithValidation() {
        var movieinfo = new MovieInfo(null, "",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));


        webTestClient.post().uri(MOVIES_INFO_URL)
                .bodyValue(movieinfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {

                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    String expectedErrMsg = "movieInfo.name must be present";

                    assert responseBody != null;

                    assertEquals(expectedErrMsg,responseBody);

                });
    }

}
