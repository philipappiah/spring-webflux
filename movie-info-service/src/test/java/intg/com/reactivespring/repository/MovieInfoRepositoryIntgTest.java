package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;


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
    void findAll(){

        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();

    }


    @Test
    void findById(){

        Mono<MovieInfo> moviesInfoFlux = movieInfoRepository.findById("abc").log();

        StepVerifier.create(moviesInfoFlux)
               // .expectNextCount(1)
                .assertNext(movieInfo -> {
                    assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();

    }

    @Test
    void saveMovieInfo(){

        var movieinfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoMono = movieInfoRepository.save(movieinfo);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins", movieInfo.getName());
                }).verifyComplete();


    }

    @Test
    void updateMovieInfo(){

        var movieinfo = movieInfoRepository.findById("abc").block();
        assert movieinfo != null;
        movieinfo.setYear(2021);

        var movieInfoMono = movieInfoRepository.save(movieinfo);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(2021, movieInfo.getYear());
                }).verifyComplete();


    }

    @Test
    void deleteMovieInfo(){

        movieInfoRepository.deleteById("abc").block();
        var moviesInfo = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfo)
                .expectNextCount(2)
                .verifyComplete();

    }


    @Test
    void findByYear(){

        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void findByName(){

        Mono<MovieInfo> moviesInfoFlux = movieInfoRepository.findByName("The Dark Knight").log();

        StepVerifier.create(moviesInfoFlux)
                 .expectNextCount(1)
                .assertNext(movieInfo -> {
                    assertEquals("The Dark Knight", movieInfo.getName());
                })
                .verifyComplete();

    }



}