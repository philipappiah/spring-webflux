package com.reactivespring.service;


import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo){

      return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovieInfos(){
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfo(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {
        return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    if(updatedMovieInfo.getName() != null) movieInfo.setName(updatedMovieInfo.getName());
                    if(updatedMovieInfo.getCast() != null) movieInfo.setCast(updatedMovieInfo.getCast());
                    if(updatedMovieInfo.getYear() != null) movieInfo.setYear(updatedMovieInfo.getYear());
                    if(updatedMovieInfo.getReleaseDate() != null ) movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    if(updatedMovieInfo.getYear() != null) movieInfo.setYear(updatedMovieInfo.getYear());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }

    public Mono<MovieInfo> getMovieInfoByName(String name){
        return movieInfoRepository.findByName(name);
    }
}
