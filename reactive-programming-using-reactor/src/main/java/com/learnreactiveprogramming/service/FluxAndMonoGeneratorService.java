package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux(){

        return Flux.fromIterable(List.of("alex", "ben", "choice")).log();
    }

    public Flux<String> namesFluxMap(){

        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .map(String::toUpperCase).log();
    }

    public Flux<String> namesFluxMapAsync(){

        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .map(String::toUpperCase).log();
    }

    public Flux<String> namesFluxImmutability(){

        var namesFlux = Flux.fromIterable(List.of("alex", "ben", "choice"));
        namesFlux.map(String::toUpperCase).log();

        return namesFlux;
    }

    public Flux<String> namesFluxFilter(){

        int len = 3;
        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .map(String::toUpperCase).filter(s -> s.length() > len)
                .log();
    }


    public Flux<String> namesFluxFlatmap(int len){
        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .map(String::toUpperCase).filter(s -> s.length() > len)
                .flatMap( t -> splitString(t))
                .log();
    }


    //ALEX -> Flux(A,L,E,X)
    public Flux<String> splitString(String name){
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    //flat map does not preserve the state of ordering
    public Flux<String> namesFluxFlatmapAsync(int len){

        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .map(String::toUpperCase).filter(s -> s.length() > len)
                .flatMap( t -> splitStringWithDelay(t))
                .log();
    }

    //concat map preserves the state of ordering
    public Flux<String> namesFluxConcatmapAsync(int len){

        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .map(String::toUpperCase).filter(s -> s.length() > len)
                .concatMap( t -> splitStringWithDelay(t))
                .log();
    }





    public Flux<String> splitStringWithDelay(String name){
        var charArray = name.split("");

        int delay = new Random().nextInt(1000);
        return Flux.fromArray(charArray).delayElements(Duration.ofMillis(delay));
    }

    public Mono<String> nameMono(){
        return Mono.just("philip").log();
    }

    public Mono<List<String>> namesMonoFlatmap(int len){

        return Mono.just("alex")
                .map(String::toUpperCase).filter(s -> s.length() > len)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMonoFlatmapMany(int len){

        return Mono.just("alex")
                .map(String::toUpperCase)
                .flatMapMany(this::splitString)
                .log();
    }

    public Flux<String> namesFluxTransform(int len){

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .map(String::toUpperCase).filter(s -> s.length() > len);


        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .transform(filterMap)
                .flatMap( t -> splitString(t))
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFluxSwitchIfEmpty(int len){

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .map(String::toUpperCase).filter(s -> s.length() > len).flatMap( t -> splitString(t));


        var defaultFlux = Flux.just("default").transform(filterMap);
        return Flux.fromIterable(List.of("alex", "ben", "choice"))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }


    public Flux<String> exploreConcat(){
        var abcFlux = Flux.just("A", "B","C");
        var defFlux = Flux.just("D", "E", "F");
        return Flux.concat(abcFlux, defFlux);
    }


    public Flux<String> exploreConcatWith(){
        var abcFlux = Mono.just("A");
        var defFlux = Flux.just("D", "E", "F");

        // instance method
        // returns a flux
        return abcFlux.concatWith(defFlux).log();
    }


    public Flux<String> exploreMerge(){
        var abcFlux = Flux.just("A", "B","C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

        // output = A, D, B, E, C, F
        return Flux.merge(abcFlux, defFlux).log();
    }

    public Flux<String> exploreMergeWith(){
        var abcFlux = Flux.just("A", "B","C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return abcFlux.mergeWith( defFlux).log();
    }

    // maintains the ordering
    public Flux<String> exploreMergeSequential(){
        var abcFlux = Flux.just("A", "B","C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));

        // output: A, B, C, D, E, F
        return Flux.mergeSequential( abcFlux, defFlux).log();
    }


    public Flux<String> exploreZip(){
        var abcFlux = Flux.just("A", "B","C");
        var defFlux = Flux.just("D", "E", "F");
        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second);//AD, BE, CF

    }

    public Flux<String> exploreZip1(){
        var abcFlux = Flux.just("A", "B","C");
        var defFlux = Flux.just("D", "E", "F");
        var _123flux = Flux.just("1", "2", "3");
        var _456flux = Flux.just("4", "5", "6");

        return Flux.zip(abcFlux, defFlux,_123flux,_456flux)
                .map(t4 -> t4.getT1()+t4.getT2()+t4.getT3()+t4.getT4());//AD14, BE25, CF36

    }

    public Flux<String> exploreZipWithFlux(){
        var abcFlux = Flux.just("A", "B","C");
        var defFlux = Flux.just("D", "E", "F");
        return abcFlux.zipWith(defFlux, (first, second) -> first + second);//AD, BE, CF

    }

    public Mono<String> exploreZipWithMono(){
        var abcMono = Mono.just("A");
        var defMono = Mono.just("B");
        return abcMono.zipWith(defMono).map(t2 -> t2.getT1() + t2.getT2()).log();

    }





    public Mono<List<String>> splitStringMono(String name){
        var charArray = name.split("");
        var list = List.of(charArray);
        return Mono.just(list);
    }

    public static void main(String[] args) {

        FluxAndMonoGeneratorService gservice = new FluxAndMonoGeneratorService();
        gservice.namesFluxFlatmap(3).subscribe(name -> System.out.println("name is " + name));
    }
}
