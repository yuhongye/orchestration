package com.cxy.orchestration.graph;

import com.cxy.functions.TriPredicate;

public class SingleTransition<C> extends AbstractTransition<C> {
    private TriPredicate<C, String, Object> transition;

    public SingleTransition(String dest, TriPredicate<C, String, Object> transition) {
        super(dest);
        this.transition = transition;
    }

    @Override
    public <R> TransitionResult test(C context, String src, R srcResult) {
        boolean trigger = transition.test(context, src, srcResult);
        return new TransitionResult(trigger, new Object[]{srcResult});
    }
}
