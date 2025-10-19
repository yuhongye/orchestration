package com.cxy.orchestration.graph;

import com.cxy.orchestration.annotations.NodeMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.CorePublisher;

import java.lang.reflect.Type;
import java.util.function.Function;

@AllArgsConstructor
public class ReactiveNode {
    @Getter
    private final String id;

    @Getter
    private final NodeMetadata metadata;

    private final Function<Object[], CorePublisher<Object>> biz;

    public CorePublisher<Object> execute(Object[] args) {
        return biz.apply(args);
    }
}
