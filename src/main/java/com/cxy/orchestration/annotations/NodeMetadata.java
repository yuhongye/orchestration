package com.cxy.orchestration.annotations;

import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

@AllArgsConstructor
public class NodeMetadata {
    private final Type returnType;

    private final List<ParameterMetadata> parametersMetadata;

    public ParameterMetadata getParameterMetadata(String from) {
        for (ParameterMetadata parent : parametersMetadata) {
            if (Objects.equals(parent.from, from)) {
                return parent;
            }
        }
        return null;
    }

    public int getFromOrder(String parent) {
        ParameterMetadata metadata = getParameterMetadata(parent);
        return metadata != null ? metadata.order : -1;
    }

    public OptionalInt getContextOrder() {
        for (ParameterMetadata parameter : parametersMetadata) {
            if (parameter.context()) {
                return OptionalInt.of(parameter.order);
            }
        }
        return OptionalInt.empty();
    }


    public int parameterSize() {
        return parametersMetadata.size();
    }

    static record ParameterMetadata(String from, int order, boolean optional, boolean context) { }
}
