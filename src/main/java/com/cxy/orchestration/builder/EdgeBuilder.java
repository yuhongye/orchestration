package com.cxy.orchestration.builder;

import lombok.NonNull;

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
    private BiPredicate<C, ?> when;

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

    public EdgeBuilder<C, REQ> when(@NonNull BiPredicate<C, ?> when) {
        this.when = when;
        return this;
    }

    public DCGBuilder<C, REQ> end() {
        return dcgBuilder;
    }
}
