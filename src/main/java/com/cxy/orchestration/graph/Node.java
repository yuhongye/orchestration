package com.cxy.orchestration.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@AllArgsConstructor
public class Node {
    @Getter
    private final String id;

    private final Function<Object[], Object> biz;

    public Object execute(Object[] args) {
        return biz.apply(args);
    }
}
