package com.cxy.reflection.demo;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Type;
import java.util.List;

public class TypeUtilsTest {
    public static void main(String[] args) {
        Type list1 = new TypeReference<List<List<String>>>() {}.getType();
        Type list2 = new TypeReference<List<String>>() {}.getType();
        Assertions.assertFalse(TypeUtils.isAssignable(list1, list2));
        list2 = new TypeReference<List<List>>() {}.getType();
        Assertions.assertFalse(TypeUtils.isAssignable(list1, list2));
        list2 = new TypeReference<List<List<Integer>>>() {}.getType();
        Assertions.assertFalse(TypeUtils.isAssignable(list1, list2));
        list2 = new TypeReference<List<List<CharSequence>>>() {}.getType();
        Assertions.assertFalse(TypeUtils.isAssignable(list1, list2));

        Type intType = Integer.TYPE;
        Class<?> numberClass = Number.class;
        Assertions.assertTrue(TypeUtils.isAssignable(intType, numberClass));
        Assertions.assertFalse(TypeUtils.isAssignable(numberClass, intType));

        Type object = Object.class;
        Assertions.assertTrue(TypeUtils.isAssignable(intType, object));
        Assertions.assertFalse(TypeUtils.isAssignable(object, intType));

        List<>
    }

    static abstract class TypeReference<T> {
        public Type getType() {
            return getClass().getGenericSuperclass();
        }
    }
}
