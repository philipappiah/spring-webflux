package com.reactivespring.controller;


import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import com.reactivespring.service.MovieInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {


    @Autowired
    private MovieInfoService movieInfoService;

    Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many().replay().all();


    @GetMapping(value = "/movieInfos/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MovieInfo> streamMovieInfo(){
        return moviesInfoSink.asFlux();
    }
    @PostMapping("/movieInfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo){
        return movieInfoService.addMovieInfo(movieInfo)
                .doOnNext(savedMovieInfo -> moviesInfoSink.tryEmitNext(savedMovieInfo));
    }


    @GetMapping("/movieInfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year){

        if(year != null){
            return movieInfoService.getMovieInfoByYear(year);
        }
        return movieInfoService.getAllMovieInfos();
    }

    @GetMapping("/movieInfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfo(@PathVariable String id){
        return movieInfoService.getMovieInfo(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/movieInfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo movieInfo, @PathVariable String id){
        return movieInfoService.updateMovieInfo(movieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/movieInfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id){
        return  movieInfoService.deleteMovieInfo(id);
    }



}
