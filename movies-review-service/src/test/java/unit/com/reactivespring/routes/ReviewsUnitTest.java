package com.reactivespring.routes;


import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isA;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    static final String REVIEWS_URL = "/v1/reviews";
    @MockBean
    private ReviewRepository reviewRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getAllReviews(){

        var reviewsList = List.of(
                new Review("63b6a69447d33668011ad252", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 1L, "The Best Movie", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        when(reviewRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        webTestClient.get().uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(4);

    }

    @Test
    void addReview(){
        Review newReview = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));


        webTestClient.post().uri(REVIEWS_URL)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assertEquals("abc", savedReview.getReviewId());
                });
    }


    @Test
    void getReview(){


        String reviewId = "123";

        when(reviewRepository.findById(isA(String.class)))
                .thenReturn(Mono.just(new Review("123", 1L, "Awesome Movie", 9.0)));


        webTestClient.get().uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review review = reviewEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(review).getReviewId() != null;
                });
    }

    @Test
    void updateReview(){
        String movieId = "abc";

        Review updatedReview = new Review(null, 1L, "Awesome Movie", 8.0);

        when(reviewRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        when(reviewRepository.findById(isA(String.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient.put().uri(REVIEWS_URL+"/{id}", movieId)
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var _updatedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert _updatedReview != null;
                    assert _updatedReview.getMovieInfoId() != null;
                    assertEquals(9.0, _updatedReview.getRating());
                });
    }


    @Test
    void deleteReview(){
        String reviewId = "abc";
        when(reviewRepository.findById(isA(String.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        when(reviewRepository.deleteById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient.delete().uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }



    @Test
    void addReviewWithValidationErr(){
        Review newReview = new Review(null, null, "Awesome Movie", -9.0);

        when(reviewRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));


        webTestClient.post().uri(REVIEWS_URL)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId must not be null,rating.negative : please pass a non-negative value");
    }

}
