package com.cxy.orchestration.graph;

import java.util.function.BiPredicate;

public class SingleTransition<C> extends AbstractTransition<C> {
    private final BiPredicate<C, Object> transition;

    public SingleTransition(String dest, BiPredicate<C, Object> transition) {
        super(dest);
        this.transition = transition;
    }

    @Override
    public <R> TransitionResult test(C context, String src, R srcResult) {
        boolean trigger = transition.test(context, srcResult);
        return new TransitionResult(trigger, new Object[]{srcResult});
    }
}
