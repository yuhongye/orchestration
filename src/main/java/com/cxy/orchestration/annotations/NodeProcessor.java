package com.cxy.orchestration.annotations;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NodeProcessor {

    // 'process' 方法和 'createExecutor' 方法与上一个答案相同。
    // 我们只需要修改/添加验证方法。

    public Map<String, Node> process(Object instance) {
        Class<?> clazz = instance.getClass();

        // 第一遍：注册 (与之前相同)
        Map<String, Method> nodeMethodRegistry = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AsNode.class)) {
                AsNode annotation = method.getAnnotation(AsNode.class);
                String nodeId = annotation.value();
                if (nodeMethodRegistry.containsKey(nodeId)) {
                    throw new IllegalArgumentException("Duplicate node ID found: " + nodeId);
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

    /**
     * 辅助方法：验证一个节点方法的所有参数 (更新版)。
     */
    private void validateMethodParameters(String currentNodeId, Method currentMethod, Map<String, Method> registry) {

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
     * 检查 'fromType' (返回值) 是否可以赋值给 'toType' (参数)。
     * 这是反射中最复杂的部分之一。
     *
     * @param fromType 期望的类型 (源/返回值)
     * @param toType   实际的类型 (目标/参数)
     * @return 如果兼容则为 true
     */
    private boolean isTypeAssignable(Type fromType, Type toType) {
        // 1. 简单情况：完全相等 (处理 List<String> vs List<String>, int vs int)
        if (toType.equals(fromType)) {
            return true;
        }

        // 2. 目标是通配符 (e.g., ? extends Number)
        if (toType instanceof WildcardType) {
            return isWildcardAssignable(fromType, (WildcardType) toType);
        }

        // 3. 目标是类型变量 (e.g., T) - 擦除为 Object 或其上界
        if (toType instanceof TypeVariable) {
            return isTypeAssignable(fromType, ((TypeVariable<?>)toType).getBounds()[0]);
        }

        // 4. 源是类型变量 (e.g., T)
        if (fromType instanceof TypeVariable) {
            return isTypeAssignable(((TypeVariable<?>)fromType).getBounds()[0], toType);
        }

        // 5. 目标和源都是 Class (非泛型) - 处理子类和自动拆装箱
        if (fromType instanceof Class<?> && toType instanceof Class<?>) {
            Class<?> fromClass = (Class<?>) fromType;
            Class<?> toClass = (Class<?>) toType;

            // 5a. 子类检查 (e.g., to=List.class, from=ArrayList.class)
            if (toClass.isAssignableFrom(fromClass)) {
                return true;
            }

            // 5b. 自动装箱/拆箱
            // to=int, from=Integer
            if (toClass.isPrimitive() && getWrapperClass(toClass).isAssignableFrom(fromClass)) {
                return true;
            }
            // to=Integer, from=int
            if (fromClass.isPrimitive() && toClass.isAssignableFrom(getWrapperClass(fromClass))) {
                return true;
            }
            return false;
        }

        // 6. 目标和源都是参数化类型 (e.g., List<String> vs ArrayList<String>)
        if (fromType instanceof ParameterizedType && toType instanceof ParameterizedType) {
            ParameterizedType fromPT = (ParameterizedType) fromType;
            ParameterizedType toPT = (ParameterizedType) toType;

            // 6a. 检查原始类型 (e.g., List vs ArrayList)
            if (!isTypeAssignable(fromPT.getRawType(), toPT.getRawType())) {
                return false;
            }

            // 6b. 检查泛型参数 (e.g., <String> vs <String>)
            Type[] fromArgs = fromPT.getActualTypeArguments();
            Type[] toArgs = toPT.getActualTypeArguments();

            if (fromArgs.length != toArgs.length) {
                return false; // 不应该发生
            }

            for (int i = 0; i < fromArgs.length; i++) {
                // 泛型是“不变的”，除非有通配符
                // e.g., List<String> 不能赋值给 List<Object>
                // 我们必须检查 'to' 是否是通配符，或者两者是否完全相等
                Type fromArg = fromArgs[i];
                Type toArg = toArgs[i];

                if (toArg instanceof WildcardType) {
                    if (!isWildcardAssignable(fromArg, (WildcardType) toArg)) {
                        return false;
                    }
                } else {
                    // 没有通配符，必须完全相等
                    if (!toArg.equals(fromArg)) {
                        return false;
                    }
                }
            }
            return true; // 原始类型兼容，且所有泛型参数都兼容
        }

        // 7. 混合情况 (非受检赋值)
        // 7a. 目标是原始类型 (e.g., to=List, from=List<String>)
        if (toType instanceof Class<?> && fromType instanceof ParameterizedType) {
            return isTypeAssignable(((ParameterizedType) fromType).getRawType(), toType);
        }
        // 7b. 源是原始类型 (e.g., to=List<String>, from=List)
        if (toType instanceof ParameterizedType && fromType instanceof Class<?>) {
            return isTypeAssignable(fromType, ((ParameterizedType) toType).getRawType());
        }

        // 其他未处理的情况 (如 GenericArrayType) 均视为不兼容
        return false;
    }

    /**
     * 辅助方法：检查一个类型是否满足通配符的边界。
     */
    private boolean isWildcardAssignable(Type fromType, WildcardType toWildcard) {
        // e.g., to = ? extends Number
        // from = Integer -> valid
        Type[] upperBounds = toWildcard.getUpperBounds();
        if (upperBounds.length > 0 && upperBounds[0] != Object.class) {
            // 检查 'from' 是否是 'upperBound' 的子类
            if (!isTypeAssignable(fromType, upperBounds[0])) {
                return false;
            }
        }

        // e.g., to = ? super Integer
        // from = Number -> valid
        Type[] lowerBounds = toWildcard.getLowerBounds();
        if (lowerBounds.length > 0) {
            // 检查 'lowerBound' 是否是 'from' 的子类 (反向)
            if (!isTypeAssignable(lowerBounds[0], fromType)) {
                return false;
            }
        }

        return true; // 边界检查通过 (或只是 <?>)
    }

    /**
     * 获取基本类型对应的包装类 (与之前相同)。
     */
    private Class<?> getWrapperClass(Class<?> primitive) {
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == double.class) return Double.class;
        if (primitive == float.class) return Float.class;
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == char.class) return Character.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == short.class) return Short.class;
        return primitive;
    }

    /**
     * 创建执行器 (与之前相同)。
     */
    private Function<Object[], Object> createExecutor(Object instance, Method method, String nodeId) {
        // ... (与上一个答案中的代码完全相同) ...
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