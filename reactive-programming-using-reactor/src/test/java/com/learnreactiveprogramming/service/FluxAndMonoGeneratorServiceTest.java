package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService famgs = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux(){

        var namesFlux = famgs.namesFlux();
        StepVerifier.create(namesFlux).expectNextCount(3).verifyComplete();
        StepVerifier.create(namesFlux).expectNext("alex", "ben", "choice").verifyComplete();

        StepVerifier.create(namesFlux).expectNext("alex").expectNextCount(2).verifyComplete();
    }

    @Test
    void namesFluxMap(){

        var namesFluxMap = famgs.namesFluxMap();
        StepVerifier.create(namesFluxMap).expectNextCount(3).verifyComplete();
        StepVerifier.create(namesFluxMap).expectNext("ALEX", "BEN", "CHOICE").verifyComplete();

        StepVerifier.create(namesFluxMap).expectNext("ALEX").expectNextCount(2).verifyComplete();
    }

    @Test
    void namesFluxImmutability(){

        var namesFluxImmutability = famgs.namesFluxImmutability();
        StepVerifier.create(namesFluxImmutability).expectNextCount(3).verifyComplete();
        StepVerifier.create(namesFluxImmutability).expectNext("ALEX", "BEN", "CHOICE").verifyComplete();

        StepVerifier.create(namesFluxImmutability).expectNext("ALEX").expectNextCount(2).verifyComplete();
    }

    @Test
    void namesFluxFlatMap() {

        int len = 3;
        var namesFluxFlatmap = famgs.namesFluxFlatmap(len);
        StepVerifier.create(namesFluxFlatmap).expectNext("A","L","E", "X","C", "H", "O", "I", "C", "E").verifyComplete();

    }

    @Test
    void namesFluxAsync() {

        int len = 3;
        var namesFluxFlatmapAsync = famgs.namesFluxFlatmapAsync(3);
        StepVerifier.create(namesFluxFlatmapAsync).expectNextCount(9).verifyComplete();

    }

    @Test
    void namesFluxConcatMapAsync() {

        int len = 3;
        var namesFluxConcatmapAsync = famgs.namesFluxConcatmapAsync(3);
        StepVerifier.create(namesFluxConcatmapAsync).expectNext("A","L","E", "X","C", "H", "O", "I", "C", "E").verifyComplete();

    }

    @Test
    void namesMonoFlatMap() {

        int len = 3;
        var namesMonoFlatmap = famgs.namesMonoFlatmap(len);
        StepVerifier.create(namesMonoFlatmap).expectNext(List.of("A", "L", "E", "X")).verifyComplete();

    }

    @Test
    void namesMonoFlatMapMany() {
        int len = 3;
        var namesMonoFlatmapMany = famgs.namesMonoFlatmapMany(len);
        StepVerifier.create(namesMonoFlatmapMany).expectNext("A", "L", "E", "X").verifyComplete();

    }

    @Test
    void namesFluxFlatTransform() {

        int len = 3;
        var namesFluxTransform = famgs.namesFluxTransform(len);
        StepVerifier.create(namesFluxTransform).expectNext("A","L","E", "X","C", "H", "O", "I", "C", "E").verifyComplete();

    }

    @Test
    void namesFluxFlatTransform1() {

        int len = 6;
        var namesFluxTransform = famgs.namesFluxTransform(len);
        StepVerifier.create(namesFluxTransform)
                .expectNext("default")
                .verifyComplete();

    }

    @Test
    void namesFluxSwitchIfEmpty() {

        int len = 6;
        var namesFluxTransform = famgs.namesFluxSwitchIfEmpty(len);
        StepVerifier.create(namesFluxTransform)
                .expectNext("D","E", "F", "A", "U", "L", "T")
                .verifyComplete();

    }

    @Test
    void exploreConcat() {


        var exploreConcat = famgs.exploreConcat();
        StepVerifier.create(exploreConcat)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();

    }

    @Test
    void exploreMerge() {


        var exploreMerge = famgs.exploreMerge();
        StepVerifier.create(exploreMerge)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();

    }

    @Test
    void exploreZip() {


        var exploreZip = famgs.exploreZip();
        StepVerifier.create(exploreZip)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();

    }

}