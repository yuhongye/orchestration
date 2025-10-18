package com.cxy.reactor.demo;

import com.cxy.reactor.FluxUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class DoOnFirstDemo {
    public static void main(String[] args) {
        Flux<String> flux = DoFirstDemo.createFlux().cache();
        FluxUtils.doOnFirst(flux, $ -> {
            log.info("start to do first.");
            DoFirstDemo.next(flux);
        }).subscribe();

        Sleeps.sleepQuiet(2000);
    }
}
