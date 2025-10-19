package com.cxy.orchestration;

import com.cxy.orchestration.annotations.AsReactiveNode;
import com.cxy.orchestration.annotations.From;
import com.cxy.orchestration.annotations.FromContext;
import com.cxy.orchestration.builder.CompledDCGBuilder;
import com.cxy.orchestration.builder.DCGBuilder;
import com.cxy.orchestration.graph.PreBuilt;
import com.cxy.orchestration.graph.StatefulGraph;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@Slf4j
public class StatefulGraphTest {
    public static void main(String[] args) throws InterruptedException {
        DCGBuilder<String, Request> builder = new DCGBuilder<>();
        builder.scanNodeFrom(new NodeDefinition())
                .contextType(String.class)
                .requestType(Request.class)
                .addMultiDownstream()
                    .from(PreBuilt.START)
                    .toEach("a")
                    .toEach("b")
                .end()
                .addMultiUpstream()
                    .from("a")
                    .from("b")
                    .to("c")
                .end()
                .addEdge("c", "d")
                .addEdge("c", "e")
                .addMultiUpstream()
                    .from("d")
                    .from("e")
                    .to("f")
                .end();
        CompledDCGBuilder<String, Request> compledDCGBuilder = builder.compile();
        StatefulGraph<String, Request> statefulGraph = compledDCGBuilder.build();

        log.info("statefulGraph={}", statefulGraph);
        String context = "STRING_AS_CONTEXT";
        statefulGraph.start(context, new Request("req-0"));
        Thread.sleep(1000);
    }

    static record Request(String reqId) { }
}
