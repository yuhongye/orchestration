package com.cxy.orchestration.builder;


import lombok.NonNull;

import java.util.function.BiPredicate;

/**
 * 当多上游时的 build 方法
 * @param <C>
 */
public class MultiParentBuilder<C, REQ> {
    private DCGBuilder<C, REQ> dcgBuilder;

    public MultiParentBuilder(DCGBuilder<C, REQ> dcgBuilder) {
        this.dcgBuilder = dcgBuilder;
    }

    public MultiParentBuilder<C, REQ> from(@NonNull String parentId) {
        return this;
    }

    public MultiParentBuilder<C, REQ> from(@NonNull String parentId, BiPredicate<C, ?> when) {
        return this;
    }

    public MultiParentBuilder<C, REQ> to(String downstream) {
        return this;
    }

    public DCGBuilder<C, REQ> end() {
        return dcgBuilder;
    }
}