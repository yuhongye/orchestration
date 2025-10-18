package com.cxy.orchestration.annotations;

import com.cxy.orchestration.graph.Node;
import org.apache.commons.lang3.reflect.TypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NodeProcessor {

    public static Map<String, Node> process(Object instance) {
        Class<?> clazz = instance.getClass();

        // 第一遍：注册 (与之前相同)
        Map<String, Method> nodeMethodRegistry = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AsReactiveNode.class)) {
                AsReactiveNode annotation = method.getAnnotation(AsReactiveNode.class);
                String nodeId = annotation.value();
                if (nodeMethodRegistry.containsKey(nodeId)) {
                    throw new IllegalArgumentException("Duplicate node ID found: " + nodeId);
                }
                if (!validateReturnIsMonoFlux(method.getReturnType())) {
                    throw new IllegalArgumentException("node " + nodeId + " return type is not Mono or Flux, its " + method.getReturnType().getName());
                }
                nodeMethodRegistry.put(nodeId, method);
            }
        }

        // 第二遍：验证与创建 (与之前相同)
        Map<String, Node> finalNodes = new HashMap<>();
        for (Map.Entry<String, Method> entry : nodeMethodRegistry.entrySet()) {
            String currentNodeId = entry.getKey();
            Method currentMethod = entry.getValue();

            // 验证逻辑被更新
            validateMethodParameters(currentNodeId, currentMethod, nodeMethodRegistry);


            Function<Object[], Object> executor = createExecutor(instance, currentMethod, currentNodeId);
            Node node = new Node(currentNodeId, executor);
            finalNodes.put(currentNodeId, node);
        }

        return finalNodes;
    }

    private static boolean validateReturnIsMonoFlux(Class<?> clazz) {
        return Mono.class.isAssignableFrom(clazz) || Flux.class.isAssignableFrom(clazz);
    }

    /**
     * 辅助方法：验证一个节点方法的所有参数 (更新版)。
     */
    private static void validateMethodParameters(String currentNodeId, Method currentMethod, Map<String, Method> registry) {

        for (Parameter param : currentMethod.getParameters()) {
            if (param.isAnnotationPresent(From.class)) {
                From fromAnnotation = param.getAnnotation(From.class);
                String sourceNodeId = fromAnnotation.value();

                Method sourceMethod = registry.get(sourceNodeId);
                if (sourceMethod == null) {
                    throw new IllegalArgumentException(
                            String.format("Node validation failed for '%s': Parameter '%s' references unknown node '%s'.",
                                    currentNodeId, param.getName(), sourceNodeId)
                    );
                }

                // --- 核心修改在这里 ---

                // 1. 获取源的泛型返回类型 (e.g., List<String>)
                Type expectedType = sourceMethod.getGenericReturnType(); // 'from' type

                // 2. 获取参数的泛型类型 (e.g., List<Integer>)
                Type actualType = param.getParameterizedType();   // 'to' type

                // 3. 检查 void
                if (expectedType == void.class || expectedType == Void.class) {
                    throw new IllegalArgumentException(
                            String.format("Node validation failed for '%s': Parameter '%s' references node '%s' which returns void.",
                                    currentNodeId, param.getName(), sourceNodeId)
                    );
                }

                // 4. 比较泛型类型是否兼容
                if (!TypeUtils.isAssignable(expectedType, actualType)) {
                    throw new IllegalArgumentException(
                            String.format("Type mismatch for node '%s': Parameter '%s' (Type: %s) "+
                                            "cannot accept result from node '%s' (Type: %s).",
                                    currentNodeId, param.getName(), actualType.getTypeName(), // 使用 .getTypeName()
                                    sourceNodeId, expectedType.getTypeName()) // 使用 .getTypeName()
                    );
                }
            }
        }
    }

    /**
     * 创建执行器 (与之前相同)。
     */
    private static Function<Object[], Object> createExecutor(Object instance, Method method, String nodeId) {
        return (args) -> {
            try {
                method.setAccessible(true);
                return method.invoke(instance, args);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access method for node: " + nodeId, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Exception thrown by node '" + nodeId + "': " + e.getTargetException().getMessage(), e.getTargetException());
            } catch (IllegalArgumentException e) {
                String expected = Arrays.toString(method.getParameterTypes());
                String actual = (args == null) ? "null" : Arrays.toString(Arrays.stream(args).map(o -> o == null ? "null" : o.getClass().getName()).toArray());
                throw new RuntimeException("Argument mismatch for node '" + nodeId + "'. Expected: " + expected + ", Got: " + actual, e);
            }
        };
    }
}