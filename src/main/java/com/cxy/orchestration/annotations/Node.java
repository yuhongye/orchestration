package com.cxy.orchestration.annotations;

import reactor.core.publisher.Mono;
import java.util.function.Function;

/**
 * 节点类。
 * 注意：根据您的示例 Lambda 和 Mono<Object> 返回类型，
 * executor 的类型已从 Function<T, Object[]> 调整为 Function<Object[], Object>。
 * 这表示它接收一个参数数组，并返回一个结果对象。
 */
class Node {
    private final String id;
    private final Function<Object[], Object> executor;

    public Node(String id, Function<Object[], Object> executor) {
        this.id = id;
        this.executor = executor;
    }

    /**
     * 执行此节点。
     * @param results 来自上游节点的输入参数数组
     * @return 包含执行结果的 Mono
     */
    public Mono<Object> execute(Object[] results) {
        // Mono.create 会立即执行 lambda。
        // executor.apply(results) 会调用我们用反射创建的函数。
        // sink.success() 会将函数的返回值（例如 "Hello, Alice!"）放入 Mono 中。
        return Mono.create(sink -> {
            try {
                // 执行包装了反射调用的函数
                Object result = executor.apply(results);
                // 发出成功信号和结果
                sink.success(result);
            } catch (Exception e) {
                // 如果 executor.apply() 抛出异常（例如反射调用失败）
                // 我们将错误传递给 Mono
                sink.error(e);
            }
        });
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Node(id='" + id + "')";
    }
}