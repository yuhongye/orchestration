package com.cxy.orchestration.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderedParents {
    private final List<String> parents = new ArrayList<>();

    public void addParent(String parent) {
        parents.add(parent);
    }

    public int getOrder(String parent) {
        for (int i = 0; i < parents.size(); i++) {
            if (Objects.equals(parents.get(i), parent)) {
                return i;
            }
        }

        return -1;
    }

    public int size() {
        return parents.size();
    }
}
