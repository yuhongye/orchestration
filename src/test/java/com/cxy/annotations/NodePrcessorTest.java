package com.cxy.annotations;

import com.cxy.orchestration.annotations.NodeProcessor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class NodePrcessorTest {
    public static void main(String[] args) throws NoSuchMethodException {
        testRemoveMono();
    }

    public static void testRemoveMono() throws NoSuchMethodException {
        Method method = NodePrcessorTest.class.getDeclaredMethod("just");
        Type returnType = method.getGenericReturnType();
        Type actual = NodeProcessor.removeMono(returnType);
        System.out.println(actual);
    }

    static Mono<String> just() {
        return Mono.just("a");
    }
}
