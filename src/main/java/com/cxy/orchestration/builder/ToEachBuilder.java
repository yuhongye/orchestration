package com.cxy.orchestration.builder;


import com.cxy.orchestration.graph.PreBuilt;
import lombok.NonNull;

import java.util.function.BiPredicate;

/**
 * 一个节点同时触发多个下游节点，多条边是独立的
 *   + --> B
 * A + --> C
 *   + --> D
 * @param <C>
 */
public class ToEachBuilder<C, REQ> {
    private final DCGBuilder<C, REQ> dcgBuilder;

    private String from;

    public ToEachBuilder(DCGBuilder<C, REQ> dcgBuilder) {
        this.dcgBuilder = dcgBuilder;
    }

    public ToEachBuilder<C, REQ> from(@NonNull String parent) {
        this.from = parent;
        return this;
    }

    public ToEachBuilder<C, REQ> toEach(@NonNull String downstream) {
        return toEach(downstream, PreBuilt.alwaysTrue());
    }

    public ToEachBuilder<C, REQ> toEach(@NonNull String downstream, @NonNull BiPredicate<C, Object> when) {
        dcgBuilder.getTransitionBuilder().addEdge(from, downstream, when);
        return this;
    }

    public DCGBuilder<C, REQ> end() {
        return dcgBuilder;
    }
}