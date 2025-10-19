package com.cxy.orchestration.graph;

import java.util.function.BiPredicate;

public interface PreBuilt {
    String START = "start";

    String END = "end";

    static <T, U> BiPredicate<T, U> alwaysTrue() {
        return ($1, $2) -> true;
    }
}
