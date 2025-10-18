package com.cxy.reactor.demo;

import reactor.core.publisher.Mono;

public class MonoThroweTest {
    public static void main(String[] args) {
        Mono<String> mono = Mono.just("a");
        mono.<Integer>handle((s, sink) -> {
            if (s.equals("a")) {
                sink.error(new RuntimeException("can not be a"));
                return;
            }
            sink.next(1);
        }).onErrorResume(e -> {
            System.out.println(e);
            return Mono.just(10);
        }).subscribe(System.out::println);

    }
}
