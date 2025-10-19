package com.cxy.orchestration.builder;

import com.cxy.orchestration.annotations.NodeMetadata;
import com.cxy.orchestration.annotations.NodeProcessor;
import com.cxy.orchestration.graph.ReactiveNode;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import java.util.Map;
import java.util.function.BiPredicate;

import static com.cxy.orchestration.graph.PreBuilt.alwaysTrue;

public class DCGBuilder<C, REQ> {
    private Object scanFromInstance;

    private Class<C> contextClass;

    private Class<REQ> requestClass;

    TransitionBuilder<C, REQ> transitionBuilder = new TransitionBuilder<>(this);

    private Map<String, ReactiveNode> id2Node;

    public static <C, REQ> DCGBuilder<C, REQ> builder() {
        return new DCGBuilder<>();
    }

    public DCGBuilder<C, REQ> scanNodeFrom(@NonNull Object instance) {
        this.scanFromInstance = instance;
        return this;
    }

    public DCGBuilder<C, REQ> contextType(@NonNull Class<C> clazz) {
        this.contextClass = clazz;
        return this;
    }

    public DCGBuilder<C, REQ> requestType(@NonNull Class<REQ> clazz) {
        this.requestClass = clazz;
        return this;
    }

    /********************** 添加边的操作 *************************/
    public DCGBuilder<C, REQ> addEdge(String from, String to) {
        return addEdge(from, to, alwaysTrue());
    }

    public DCGBuilder<C, REQ> addEdge(String from, String to, BiPredicate<C, Object> when) {
        transitionBuilder.addEdge(from, to, when);
        return this;
    }

    public EdgeBuilder<C, REQ> addEdge() {
        return new EdgeBuilder<>(this);
    }

    public ToEachBuilder<C, REQ> addMultiDownstream() {
        return new ToEachBuilder<>(this);
    }

    public MultiParentBuilder<C, REQ> addMultiUpstream() {
        return new MultiParentBuilder<>(this);
    }
    /********************** 添加边操作结束 *************************/

    TransitionBuilder<C, REQ> getTransitionBuilder() {
        return transitionBuilder;
    }

    NodeMetadata getNodeMetadata(String nodeId) {
        return id2Node.get(nodeId).getMetadata();
    }


    public CompledDCGBuilder<C, REQ> compile() {
        check();
        initNodes();

        return new CompledDCGBuilder<>(id2Node, transitionBuilder);
    }

    void check() {
        Preconditions.checkNotNull(scanFromInstance, "scanFromInstance is null");
        Preconditions.checkNotNull(contextClass, "contextClass is null");
        Preconditions.checkNotNull(requestClass, "requestClass is null");
    }

    void initNodes() {
        id2Node = NodeProcessor.process(scanFromInstance);
    }
}
