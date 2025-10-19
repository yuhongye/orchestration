package com.cxy.orchestration.builder;

import com.cxy.orchestration.annotations.NodeMetadata;
import com.cxy.orchestration.graph.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

class TransitionBuilder<C, REQ> {
    private final DCGBuilder<C, REQ> dcgBuilder;

    /**
     * key: 下游节点； value: 所有指向下游节点的transition
     */
    private final Map<String, List<Map<String, BiPredicate<C, Object>>>> dest2Parents = new HashMap<>();

    public TransitionBuilder(DCGBuilder<C, REQ> dcgBuilder) {
        this.dcgBuilder = dcgBuilder;
    }

    public TransitionBuilder<C, REQ> addEdge(String from, String to, BiPredicate<C, Object> when) {
        List<Map<String, BiPredicate<C, Object>>> parents = dest2Parents.computeIfAbsent(to, k -> new ArrayList<>());
        parents.add(Map.of(from, when));
        return this;
    }

    public TransitionBuilder<C, REQ> addMultiParent(Map<String, BiPredicate<C, Object>> parent, String to) {
        List<Map<String, BiPredicate<C, Object>>> parents = dest2Parents.computeIfAbsent(to, k -> new ArrayList<>());
        parents.add(parent);
        return this;
    }

    Map<String, List<Transition<C>>> build() {
        Map<String, List<Transition<C>>> id2Transitions = new HashMap<>(dest2Parents.size() * 2);
        Consumer<Transition<C>> linkFromTransition = transition -> {
            transition.parents().forEach(id -> {
                List<Transition<C>> transitions = id2Transitions.computeIfAbsent(id, k -> new ArrayList<>());
                transitions.add(transition);
            });
        };

        dest2Parents.forEach((dest, parents) -> {
            NodeMetadata orderedParents = dcgBuilder.getNodeMetadata(dest);
            parents.stream()
                    .map(parent -> new Transition<>(dest, orderedParents, parent))
                    .forEach(linkFromTransition);
        });

        return id2Transitions;
    }
}
