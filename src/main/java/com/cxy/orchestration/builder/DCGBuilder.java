package com.cxy.orchestration.builder;

import com.cxy.orchestration.annotations.OrderedParents;
import com.cxy.orchestration.graph.ReactiveNode;
import com.cxy.orchestration.graph.Transition;
import com.cxy.orchestration.graph.TransitionResult;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class DCGBuilder<C, REQ> {
    private Object scanFromInstance;

    private Class<C> contextClass;

    private Class<REQ> requestClass;

    private Map<String, ReactiveNode> id2Node;

    private TransitionBuilder transitionBuilder = new TransitionBuilder();

    private Map<String, OrderedParents> id2OrderedParents;

    public static <C, REQ> DCGBuilder<C, REQ> builder() {
        return new DCGBuilder<>();
    }

    public DCGBuilder<C, REQ> scanNodeFrom(Object instance) {
        Preconditions.checkNotNull(instance, "scan instance must be not null")
        this.scanFromInstance = instance;
        return this;
    }

    public DCGBuilder<C, REQ> contextType(Class<C> clazz) {
        Preconditions.checkNotNull(clazz, "context class must be not null");
        this.contextClass = clazz;
        return this;
    }

    public DCGBuilder<C, REQ> requestType(Class<REQ> clazz) {
        Preconditions.checkNotNull(clazz, "request class must be not null");
        this.requestClass = clazz;
        return this;
    }

    public DCGBuilder<C, REQ> addEdge(String from, String to) {
        return addEdge(from, to, alwaysTrue());
    }

    public DCGBuilder<C, REQ> addEdge(String from, String to, BiPredicate<C, ?> when) {

    }

    public DCGBuilder<C, REQ> addEdge() {
    }



    BiPredicate<C, ?> alwaysTrue() {
        return ($1, $2) -> true;
    }


    class TransitionBuilder {

        Map<String, List<Transition<C>>> build() {
            return null;
        }

        public TransitionBuilder addEdge(String from, String to, BiPredicate<C, ?> when) {
            return this;
        }

    }

}
