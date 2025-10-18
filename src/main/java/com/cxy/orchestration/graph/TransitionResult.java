package com.cxy.orchestration.graph;

public record TransitionResult(boolean trigger, Object[] parentResults) { }
