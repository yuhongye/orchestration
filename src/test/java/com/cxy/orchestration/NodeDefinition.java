package com.cxy.orchestration;

import com.cxy.orchestration.annotations.AsReactiveNode;
import com.cxy.orchestration.annotations.From;
import com.cxy.orchestration.annotations.FromContext;
import com.cxy.orchestration.graph.PreBuilt;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class NodeDefinition {
    @AsReactiveNode(PreBuilt.START)
    Mono<StatefulGraphTest.Request> start() {
        return null;
    }

    @AsReactiveNode("a")
    Mono<String> a(@FromContext String ctx, @From(PreBuilt.START) StatefulGraphTest.Request request) {
        log.info("start to run a, context: {}, request: {}", ctx, request);
        return Mono.just("a");
    }

    @AsReactiveNode("b")
    Mono<String> b(@FromContext String ctx, @From(PreBuilt.START) StatefulGraphTest.Request request) {
        log.info("start to run b, context: {}, request: {}", ctx, request);
        return Mono.just("b");
    }

    @AsReactiveNode("c")
    Mono<String> b(@FromContext String ctx, @From("a") String a, @From("b") String b) {
        log.info("start to run c, context: {}, a: {}, b: {}", ctx, a, b);
        return Mono.just("c");
    }

    @AsReactiveNode("d")
    Mono<String> d(@FromContext String ctx, @From("c") String c) {
        log.info("start to run d, context: {}, c: {}", ctx, c);
        return Mono.just("d");
    }

    @AsReactiveNode("e")
    Mono<String> e(@FromContext String ctx, @From("c") String c) {
        log.info("start to run e, context: {}, c: {}", ctx, c);
        return Mono.just("e");
    }

    @AsReactiveNode("f")
    Mono<String> f(@FromContext String ctx, @From("d") String d, @From("e") String e) {
        log.info("start to run f, context: {}, d: {}, e: {}", ctx, d, e);
        return Mono.just("f");
    }
}
