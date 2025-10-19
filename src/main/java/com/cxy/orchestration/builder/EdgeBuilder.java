package com.cxy.orchestration.builder;

import com.cxy.orchestration.graph.PreBuilt;
import lombok.NonNull;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * A -> B 的形式
 * @param <C>
 * @param <REQ>
 */
public class EdgeBuilder<C, REQ> {
    private final DCGBuilder<C, REQ> dcgBuilder;

    private String from;
    private String to;
    private BiPredicate<C, Object> when = PreBuilt.alwaysTrue();

    public EdgeBuilder(DCGBuilder<C, REQ> dcgBuilder) {
        this.dcgBuilder = dcgBuilder;
    }

    public EdgeBuilder<C, REQ> from(@NonNull String from) {
        this.from = from;
        return this;
    }

    public EdgeBuilder<C, REQ> to(@NonNull String to) {
        this.to = to;
        return this;
    }

    public EdgeBuilder<C, REQ> when(@NonNull BiPredicate<C, Object> when) {
        this.when = when;
        return this;
    }

    public DCGBuilder<C, REQ> end() {
        dcgBuilder.getTransitionBuilder().addEdge(from, to, when);
        return dcgBuilder;
    }
}
