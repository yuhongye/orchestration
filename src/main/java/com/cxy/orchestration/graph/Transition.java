package com.cxy.orchestration.graph;

public interface Transition<C> {
    String dest();

    /**
     *
     * @param context 请求级别上下文
     * @param src parent 节点
     * @param srcResult parent 节点的结果
     * @return 是否可以从 src -> dest
     * @param <R>
     */
    <R> TransitionResult test(C context, String src, R srcResult);
}
