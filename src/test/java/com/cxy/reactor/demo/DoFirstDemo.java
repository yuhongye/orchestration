package com.cxy.reactor.demo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;


@Slf4j
public class DoFirstDemo {
    static FluxSink<String> sink;
    public static void main(String[] args) {
        Flux<String> flux = createFlux();
//        new Thread(() -> {
//            Sleeps.sleepQuiet(500);
//            for (int i = 0; i < 10; i++) {
//                sink.next("e-" + i);
//                log.info("sink e-{}", i);
//            }
//            sink.complete();
//        }).start();

        flux.doFirst(() -> {
            log.info("start to do first.");
            next(flux);
        }).subscribe();
        Sleeps.sleepQuiet(2000);
    }

    public static Flux<String> createFlux() {
        return Flux.just("1", "2", "3", "4", "5");
//        return Flux.create(sink1 -> {
//            sink = sink1;
//        });
    }

    public static void next(Flux<String> flux) {
        log.info("Start do next node.");
        flux.toStream().forEach(e -> log.info("consume in next node: {}", e));
    }
}
