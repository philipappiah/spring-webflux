package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink(){
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1 : " + integer);
        });

        Flux<Integer> integerFlux1 = replaySink.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2 : " + integer);
        });

        replaySink.tryEmitNext(3);
    }


    @Test
    void sinks_multicast(){
        Sinks.Many<Integer> multiCast = Sinks.many().multicast().onBackpressureBuffer();

        multiCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multiCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = multiCast.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1 : " + integer);
        });

        Flux<Integer> integerFlux1 = multiCast.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2 : " + integer);
        });

        multiCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);


    }


    @Test
    void sinks_unicast(){
        Sinks.Many<Integer> uniCast = Sinks.many().unicast().onBackpressureBuffer();

        uniCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        uniCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = uniCast.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1 : " + integer);
        });

        Flux<Integer> integerFlux1 = uniCast.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2 : " + integer);
        });

        uniCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);


    }

}
