package com.cxy.functions;

@FunctionalInterface
public interface TriPredicate<A, B, C> {
    boolean test(A a, B b, C c);
}
