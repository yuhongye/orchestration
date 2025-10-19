package com.cxy.orchestration.graph;

import com.cxy.orchestration.annotations.NodeMetadata;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class Transition<C> {
    private final String dest;

    /**
     * 所有的上游节点，上游节点已按照参数顺序排好
     */
    private final NodeMetadata metadata;

    /**
     *
     */
    private final Map<String, BiPredicate<C, Object>> id2Condition;

    /**
     * 上游节点的结果，顺序与{@link #metadata}一直
     */
    private Object[] results;
    private int finishedNum = 0;
    private boolean test = true;

    public Transition(String dest, NodeMetadata metadata, Map<String, BiPredicate<C, Object>> id2Condition) {
        this.dest = dest;
        this.metadata = metadata;
        this.id2Condition = id2Condition;
        this.results = new Object[metadata.parameterSize()];
    }

    public Set<String> parents() {
        return id2Condition.keySet();
    }

    public String dest() {
        return dest;
    }

    public synchronized <R> TransitionResult test(C context, String src, R srcResult) {
        int order = metadata.getFromOrder(src);
        results[order] = srcResult;
        finishedNum++;

        BiPredicate<C, Object> transition = id2Condition.get(src);
        boolean pass = transition.test(context, srcResult);
        test = test && pass;
        boolean allPass = test && finishedNum == id2Condition.size();
        metadata.getContextOrder().ifPresent(i -> results[i] = context);
        TransitionResult transitionResult = new TransitionResult(allPass, results);

        if (allPass) {
            reset();
        }
        return transitionResult;
    }

    private void reset() {
        results = new Object[metadata.parameterSize()];
        test = false;
    }
}
