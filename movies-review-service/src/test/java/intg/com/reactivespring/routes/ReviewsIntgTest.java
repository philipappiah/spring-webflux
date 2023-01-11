package com.reactivespring.routes;


import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewRepository;
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
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    static final String REVIEWS_URL = "/v1/reviews";
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewRepository reviewRepository;

    @BeforeEach
    void setUp(){

        var reviewsList = List.of(
                new Review("63b6a69447d33668011ad252", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 1L, "The Best Movie", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        reviewRepository.deleteAll().block();
    }

    @Test
    void  addReview(){

       Review newReview = new Review(null, 1L, "Awesome Movie", 9.0);

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
                });
    }

    @Test
    void getReviews(){
        webTestClient.get().uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(4);
    }

    @Test
    void getReview(){

        String reviewId = "63b6a69447d33668011ad252";
        webTestClient.get().uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review review = reviewEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(review).getReviewId() != null;
                    assertEquals(reviewId,review.getReviewId());
                });

    }

    @Test
    void updateReview(){

        Review review = new Review("63b6a69447d33668011ad252",1L, "Awesome Movie Updated", 9.0);

        String reviewId = "63b6a69447d33668011ad252";
        webTestClient.put().uri(REVIEWS_URL+"/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                   Review review1 = reviewEntityExchangeResult.getResponseBody();
                    assert review1 != null;
                    assertEquals("Awesome Movie Updated", review1.getComment());
                });



    }

    @Test
    void getReviewsByMovieInfoId(){
        URI uriStr = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1)
                .buildAndExpand().toUri();
        webTestClient.get().uri(uriStr)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);


    }


    @Test
    void deleteReview(){
        String reviewId = "63b6a69447d33668011ad252";
        webTestClient.delete().uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();

    }

}
