package com.cxy.orchestration.graph;

import com.cxy.functions.TriPredicate;

import java.util.HashMap;
import java.util.Map;

/**
 * 多个上游同时满足才能触发下游节点
 * @param <C>
 */
public class MultiTransition<C> extends AbstractTransition<C> {
    private boolean test = false;

    private final Map<String, TriPredicate<C, String, Object>> nodeId2Transition;

    private final Map<String, Object> nodeId2Result;

    public MultiTransition(String dest, Map<String, TriPredicate<C, String, Object>> nodeId2Transition) {
        super(dest);
        this.nodeId2Transition = nodeId2Transition;
        this.nodeId2Result = new HashMap<>(nodeId2Transition.size() * 2);
    }
    public synchronized <R> TransitionResult test(C context, String src, R srcResult) {
        reset();
        TriPredicate<C, String,  Object> transition = nodeId2Transition.get(src);
        boolean pass = transition.test(context, src, srcResult);
        if (pass) {
            nodeId2Result.put(src, srcResult);
        } else {
            test = false;
        }
        boolean allPass = test && nodeId2Result.size() == nodeId2Transition.size();
        if (allPass) {
            reset();
        }
        return new TransitionResult(allPass, null);
    }

    private void reset() {
        this.nodeId2Result.clear();
        test = false;
    }
}
