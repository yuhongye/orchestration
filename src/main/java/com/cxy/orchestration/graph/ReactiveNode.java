package com.cxy.orchestration.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.CorePublisher;

import java.util.function.Function;

@AllArgsConstructor
public class ReactiveNode {
    @Getter
    private final String id;

    private final Function<Object[], CorePublisher<Object>> biz;

    public CorePublisher<Object> execute(Object[] args) {
        return biz.apply(args);
    }
}
