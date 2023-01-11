package com.reactivespring.handler;


import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private Validator validator;
    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.info("constraints violation : {}", constraintViolations);
        if(constraintViolations.size() > 0) {
            String errorMessage = constraintViolations.stream().map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }

    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {

        Optional<String> movieInfoId = serverRequest.queryParam("movieInfoId");
        if(movieInfoId.isPresent()) {
            Flux<Review> reviews  = reviewRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewResponse(reviews);
        }
            Flux<Review> reviews = reviewRepository.findAll();
            return buildReviewResponse(reviews);

    }

    private static Mono<ServerResponse> buildReviewResponse(Flux<Review> reviews) {
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> getReview(ServerRequest serverRequest) {
        String reviewId = serverRequest.pathVariable("id");
        Mono<Review> review = reviewRepository.findById(reviewId);
        return ServerResponse.ok().body(review, Review.class).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
        String reviewId = serverRequest.pathVariable("id");

        Mono<Review> reviewUpdate = serverRequest.bodyToMono(Review.class);
                //.switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id "+reviewId)));

        Mono<Review> existingReview = reviewRepository.findById(reviewId);

        return existingReview.flatMap(review -> reviewUpdate.map(review1 -> {
                    if(review1.getComment() != null)  review.setComment(review1.getComment());
                    if(review1.getRating() != null) review.setRating(review1.getRating());
                    return review ;
                }).flatMap(reviewRepository::save).flatMap(ServerResponse.ok()::bodyValue))
               .switchIfEmpty(ServerResponse.notFound().build());



    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        String reviewId = serverRequest.pathVariable("id");

        return reviewRepository.findById(reviewId)
                        .flatMap(review -> reviewRepository.deleteById(reviewId))
                                .then(ServerResponse.noContent().build());
    }

}
