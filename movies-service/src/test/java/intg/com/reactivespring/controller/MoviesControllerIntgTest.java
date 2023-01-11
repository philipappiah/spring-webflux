package com.reactivespring.controller;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 5656)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:5656/v1/movieInfos",
                "restClient.reviewsUrl=http://localhost:5656/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

        final static String MOVIES_URL = "/v1/movies";
        @Autowired
        WebTestClient webTestClient;

        @Test
        void retrieveMovieById(){

                String movieId = "abc";


                stubFor(get(urlEqualTo("/v1/movieInfos"+"/"+movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                        ));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                        ));

                webTestClient.get().uri(MOVIES_URL+"/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody(Movie.class)
                        .consumeWith(movieEntityExchangeResult -> {
                                var movie = movieEntityExchangeResult.getResponseBody();
                                assert(Objects.requireNonNull(movie).getReviewList().size() == 2);
                                assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        })
                ;

        }

        @Test
        void retrieveMovieById_404_movieInfo(){

                String movieId = "abc";


                stubFor(get(urlEqualTo("/v1/movieInfos"+"/"+movieId))
                        .willReturn(aResponse()
                                .withStatus(404)
                        ));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                        ));

                webTestClient.get().uri(MOVIES_URL+"/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .is4xxClientError()
                        .expectBody(String.class)
                        .isEqualTo("There is no MovieInfo Available for the passed Id: "+movieId)
                ;

        }

        @Test
        void retrieveMovieById_404_reviews(){

                String movieId = "abc";


                stubFor(get(urlEqualTo("/v1/movieInfos"+"/"+movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                        ));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withStatus(404)
                        ));

                webTestClient.get().uri(MOVIES_URL+"/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody(Movie.class)
                        .consumeWith(movieEntityExchangeResult -> {
                                var movie = movieEntityExchangeResult.getResponseBody();
                                assert(Objects.requireNonNull(movie).getReviewList().size() == 0);
                                assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        })
                ;

        }

        @Test
        void retrieveMovieById_5XX_movieInfo(){

                String movieId = "abc";


                stubFor(get(urlEqualTo("/v1/movieInfos"+"/"+movieId))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("Server Exception in MoviesInfoService MovieInfo Service Unavailable")
                        ));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                        ));

                webTestClient.get().uri(MOVIES_URL+"/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .is5xxServerError()
                        .expectBody(String.class)
                        .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable")
                ;

                WireMock.verify(4,getRequestedFor(urlEqualTo("/v1/movieInfos"+"/"+movieId)));

        }


        @Test
        void retrieveReviewsById_5XX_Reviews(){

                String movieId = "abc";

                stubFor(get(urlPathEqualTo("/v1/movieInfos"+"/"+movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                        ));

                stubFor(get(urlEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("Review Service Not Available")
                        ));



                webTestClient.get().uri(MOVIES_URL+"/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .is5xxServerError()
                        .expectBody(String.class)
                        .isEqualTo("Server Exception in ReviewsService Review Service Unavailable")
                ;

                WireMock.verify(4,getRequestedFor(urlEqualTo("/v1/reviews*")));

        }

}
