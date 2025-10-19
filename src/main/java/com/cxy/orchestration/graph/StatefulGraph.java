package com.cxy.orchestration.graph;

import lombok.extern.slf4j.Slf4j;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
public class StatefulGraph<C, REQ> {
    private final String root = PreBuilt.START;

    private final Map<String, ReactiveNode> id2Node;

    private final Map<String, List<Transition<C>>> id2Downstreams;

    public StatefulGraph(Map<String, ReactiveNode> id2Node, Map<String, List<Transition<C>>> id2Downstreams) {
        this.id2Node = id2Node;
        this.id2Downstreams = id2Downstreams;
    }

    public void start(C context, REQ request) {
        trigger(context, root, request);
    }

    private void trigger(C context, String src, Object srcResult) {
        List<Transition<C>> downstreams = id2Downstreams.get(src);
        if (downstreams == null) {
            log.info("src node is {}, its no downstreams", src);
            return;
        }
        for (Transition<C> downstream : downstreams) {
            TransitionResult transitionResult = downstream.test(context, src, srcResult);
            if (transitionResult.trigger()) {
                String dest = downstream.dest();
                ReactiveNode node = id2Node.get(dest);
                CorePublisher<Object> future = node.execute(transitionResult.parentResults());
                registerWhenFinish(context, node.getId(), future);
            }
        }
    }

    private void registerWhenFinish(C context, String src, CorePublisher<Object> future) {
        if (future instanceof Mono<Object> mono) {
            monoResult(context, src, mono);
        } else if (future instanceof Flux<Object> flux) {
            fluxResult(context, src, flux);
        } else {
            throw new RuntimeException("unknown type of " + future.getClass().getName());
        }
    }
    private void monoResult(C context, String src, Mono<Object> srcResult) {
        srcResult.subscribe(result -> trigger(context, src, result));
    }

    private void fluxResult(C context, String src, Flux<Object> srcResult) {
        srcResult.doFirst(() -> {
            trigger(context, src, srcResult);
        }).subscribe();
    }
}
