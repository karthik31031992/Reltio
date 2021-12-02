package com.reltio.cst.dataload.impl;

import java.util.*;

class LoadBatch<T> {

    private List<T> objects = new ArrayList<>();
    private String uuid = UUID.randomUUID().toString();

    LoadBatch(List<T> list) {
        objects.addAll(list);
    }

    LoadBatch() {
    }

    void addAll(Collection<T> values) {
        objects.addAll(values);
    }

    void add(T value) {
        objects.add(value);
    }

    List<T> getObjects() {
        return objects;
    }

    int size() {
        return objects.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadBatch<?> loadBatch = (LoadBatch<?>) o;
        return uuid.equals(loadBatch.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
