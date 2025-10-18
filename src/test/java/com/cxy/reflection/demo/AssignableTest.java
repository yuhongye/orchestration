package com.cxy.reflection.demo;

import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssignableTest {
    public static void main(String[] args) throws NoSuchMethodException {
        assertFalse(S.class.isAssignableFrom(F.class));
        Method f = S.class.getDeclaredMethod("f");
        Class<?> returnType = f.getReturnType();
        System.out.println(returnType);
        assertTrue(Mono.class.isAssignableFrom(returnType));
    }

    static class F {}

    static class S extends F {
        Mono<String> f() {
            return null;
        }
    }
}
