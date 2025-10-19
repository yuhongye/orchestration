package com.cxy.orchestration.builder;


import com.cxy.orchestration.graph.PreBuilt;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * 当多上游时的 build 方法
 * @param <C>
 */
public class MultiParentBuilder<C, REQ> {
    private final DCGBuilder<C, REQ> dcgBuilder;

    private final Map<String, BiPredicate<C, Object>> parents = new HashMap<>();

    private String to;

    public MultiParentBuilder(DCGBuilder<C, REQ> dcgBuilder) {
        this.dcgBuilder = dcgBuilder;
    }

    public MultiParentBuilder<C, REQ> from(@NonNull String parentId) {
        return from(parentId, PreBuilt.alwaysTrue());
    }

    public MultiParentBuilder<C, REQ> from(@NonNull String parentId, BiPredicate<C, Object> when) {
        parents.put(parentId, when);
        return this;
    }

    public MultiParentBuilder<C, REQ> to(@NonNull String downstream) {
        this.to = downstream;
        return this;
    }

    public DCGBuilder<C, REQ> end() {
        dcgBuilder.getTransitionBuilder().addMultiParent(parents, to);
        return dcgBuilder;
    }
}