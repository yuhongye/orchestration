package com.cxy.reflection.demo;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenericTest {

    public static void main(String[] args) throws NoSuchMethodException {
        Method method = A.class.getDeclaredMethod("f", Map.class);
        System.out.println(method);
        Type returnType = method.getGenericReturnType();
        System.out.println(Arrays.toString(((ParameterizedType) returnType).getActualTypeArguments()));;


        System.out.println("------------------- Parameter info:");
        for (Type type : method.getGenericParameterTypes()) {
            dispatchType(type);
        }
    }

    public static void dispatchType(Type type) {
        System.out.println("Type: " + type.getTypeName());
        if (! (type instanceof ParameterizedType parameterizedType)) {
            return;
        }

        System.out.println("Parameter type: " + parameterizedType.getTypeName());

        // 获取参数中的泛型类型
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        for (Type actualType : actualTypeArguments) {
            System.out.println("  - Parameter Generic Type: " + actualType.getTypeName());
            if (actualType instanceof ParameterizedType innerType) {
                for (Type innerActualType : innerType.getActualTypeArguments()) {
                    System.out.println("    -- Inner Generic Type: " + innerActualType.getTypeName());
                }
            }
        }
    }

    public static class A {
        public List<List<String>> f(Map<String, List<List<String>>> arg) {
            System.out.println(arg);
            return arg.get("key");
        }
    }
}
