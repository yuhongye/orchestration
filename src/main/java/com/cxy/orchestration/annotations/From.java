package com.cxy.orchestration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法参数，表示其值来自于另一个节点。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface From {
    /**
     * 依赖的源节点ID
     */
    String value();

    /**
     * 这个参数是否为可选，当可选时，可能会传递null
     * @return
     */
    boolean optional();
}