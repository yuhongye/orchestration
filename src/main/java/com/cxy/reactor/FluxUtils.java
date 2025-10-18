package com.cxy.reactor;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FluxUtils {
    public static <T> Flux<T> doOnFirst(Flux<T> source, Consumer<T> onFirst) {
        AtomicBoolean first = new AtomicBoolean(true);
        return source.doOnNext(v -> {
            if (first.compareAndSet(true, false)) {
                onFirst.accept(v);
            }
        });
    }
}
