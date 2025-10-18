package com.cxy.orchestration.graph;

public abstract class AbstractTransition<C> implements Transition<C> {
    /**
     * 下游节点
     */
    private final String dest;

    public AbstractTransition(String dest) {
        this.dest = dest;
    }

    @Override
    public String dest() {
        return dest;
    }
}
