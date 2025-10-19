package com.cxy.orchestration.graph;

import com.cxy.orchestration.annotations.OrderedParents;

import java.util.function.BiPredicate;

/**
 * 多个上游同时满足才能触发下游节点
 * @param <C>
 */
public class MultiTransition<C> extends AbstractTransition<C> {
    private boolean test = false;

    /**
     * 所有的上游节点，上游节点已按照参数顺序排好
     */
    private final OrderedParents orderedParents;

    /**
     * 上游节点对应的transition, 顺序与 {@link #orderedParents} 一致
     */
    private final BiPredicate<C, Object>[] transitions;

    /**
     * 上游节点的结果，顺序与{@link #orderedParents}一直
     */
    private Object[] results;
    private int finishedNum = 0;

    public MultiTransition(String dest, OrderedParents orderedParents, BiPredicate<C, Object>[] transitions) {
        super(dest);
        if (orderedParents.size() != transitions.length) {
            throw new IllegalArgumentException("orderedParents size is not equals to transitions size:" + transitions.length);
        }
        this.orderedParents = orderedParents;
        this.transitions = transitions;
        this.results = new Object[orderedParents.size()];
    }
    public synchronized <R> TransitionResult test(C context, String src, R srcResult) {
        int order = orderedParents.getOrder(src);
        results[order] = srcResult;
        finishedNum++;

        BiPredicate<C, Object> transition = transitions[order];
        boolean pass = transition.test(context, srcResult);
        test = test && pass;
        boolean allPass = test && finishedNum == orderedParents.size();
        TransitionResult transitionResult = new TransitionResult(allPass, results);

        if (allPass) {
            reset();
        }
        return transitionResult;
    }

    private void reset() {
        results = new Object[orderedParents.size()];
        test = false;
    }
}
