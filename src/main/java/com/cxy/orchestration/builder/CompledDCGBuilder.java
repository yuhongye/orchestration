package com.cxy.orchestration.builder;

import com.cxy.orchestration.graph.ReactiveNode;
import com.cxy.orchestration.graph.StatefulGraph;
import com.cxy.orchestration.graph.Transition;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CompledDCGBuilder<C, REQ> {
    private final Map<String, ReactiveNode> id2Node;

    private final TransitionBuilder<C, REQ> transitionBuilder;

    public StatefulGraph<C, REQ> build() {
        Map<String, List<Transition<C>>> id2Transitions = transitionBuilder.build();
        return new StatefulGraph<>(id2Node, id2Transitions);
    }
}
