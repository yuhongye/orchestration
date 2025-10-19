package com.cxy.orchestration.annotations;

import com.cxy.orchestration.graph.ReactiveNode;
import org.apache.commons.lang3.reflect.TypeUtils;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

public class NodeProcessor {

    public static Map<String, ReactiveNode> process(Object instance) {
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
                validateReturnIsMonoFlux(nodeId, method);
                validateParamterAnnotated(nodeId, method);
                nodeMethodRegistry.put(nodeId, method);
            }
        }

        // 第二遍：验证与创建
        Map<String, ReactiveNode> finalNodes = new HashMap<>();
        for (Map.Entry<String, Method> entry : nodeMethodRegistry.entrySet()) {
            String nodeId = entry.getKey();
            Method method = entry.getValue();

            // 验证逻辑被更新
            validateMethodParameters(nodeId, method, nodeMethodRegistry);


            Function<Object[], CorePublisher<Object>> executor = createExecutor(instance, method, nodeId);
            NodeMetadata metadata = createNodeMetadata(nodeId, method);
            ReactiveNode node = new ReactiveNode(nodeId, metadata, executor);
            finalNodes.put(nodeId, node);
        }

        return finalNodes;
    }

    private static void validateReturnIsMonoFlux(String nodeId, Method method) {
        Class<?> returnType = method.getReturnType();
        boolean isReactive = Mono.class.isAssignableFrom(returnType) || Flux.class.isAssignableFrom(returnType);
        if (!isReactive) {
            throw new IllegalArgumentException("node " + nodeId + " return type is not Mono or Flux, its " + method.getReturnType().getName());
        }
    }

    private static void validateParamterAnnotated(String nodeId, Method method) {
        for (Parameter param : method.getParameters()) {
            if (!(param.isAnnotationPresent(From.class) || param.isAnnotationPresent(FromContext.class))) {
                String paramName = param.getName();
                throw new IllegalArgumentException("node " + nodeId + " param name " + paramName + " is not annotated by @From or @FromContext");
            }
        }
    }

    /**
     * 辅助方法：验证一个节点方法的所有参数
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

                // 1. 获取源的泛型返回类型 (e.g., List<String>)
                Type expectedType = removeMono(sourceMethod.getGenericReturnType()); // 'from' type

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

    public static Type removeMono(Type type) {
        if (type instanceof ParameterizedType ptype) {
            if (TypeUtils.isAssignable(ptype.getRawType(), Mono.class)) {
                return ptype.getActualTypeArguments()[0];
            }
        }
        return type;
    }

    /**
     * 创建执行器 (与之前相同)。
     */
    private static Function<Object[], CorePublisher<Object>> createExecutor(Object instance, Method method, String nodeId) {
        return (args) -> {
            try {
                method.setAccessible(true);
                return (CorePublisher<Object>) method.invoke(instance, args);
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

    private static NodeMetadata createNodeMetadata(String nodeId, Method method) {
        Parameter[] parameters = method.getParameters();
        List<NodeMetadata.ParameterMetadata> paramMetadatas = new ArrayList<>(parameters.length);
        int order = 0;
        for (Parameter param : parameters) {
            if (param.isAnnotationPresent(From.class)) {
                From fromAnnotation = param.getAnnotation(From.class);
                String fromNodeId = fromAnnotation.value();
                boolean isOptional = fromAnnotation.optional();
                paramMetadatas.add(new NodeMetadata.ParameterMetadata(fromNodeId, order, isOptional, false));
            } else if (param.isAnnotationPresent(FromContext.class)) {
                paramMetadatas.add(new NodeMetadata.ParameterMetadata(null, order, false, true));
            } else {
                throw new IllegalArgumentException("node " + nodeId + " method " + method.getName() + " parameter " + param.getName() + " is not annotated by @From or @FromContext");
            }
            order++;
        }

        return new NodeMetadata(method.getGenericReturnType(), paramMetadatas);
    }
}