package com.cxy.orchestration.builder;


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
    private String from;

    private DCGBuilder<C, REQ> dcgBuilder;

    public ToEachBuilder(DCGBuilder<C, REQ> dcgBuilder) {
        this.dcgBuilder = dcgBuilder;
    }

    public ToEachBuilder<C, REQ> from(@NonNull String parent) {
        this.from = parent;
        return this;
    }

    public ToEachBuilder<C, REQ> toEach(@NonNull String downstream) {
        return this;
    }

    public ToEachBuilder<C, REQ> toEach(@NonNull String downstream, @NonNull BiPredicate<C, ?> when) {
        return this;
    }

    public DCGBuilder<C, REQ> end() {
        return dcgBuilder;
    }