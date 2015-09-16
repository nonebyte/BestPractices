package com.bestpractices.base.collection;

import android.util.SparseArray;

public class HashMapSlim<V> {
    private final SparseArray<V> mArray;

    public HashMapSlim() {
        this(10);
    }

    public HashMapSlim(int initCapacity) {
        mArray = new SparseArray<V>(initCapacity);
    }

    public void put(Object key, V value) {
        mArray.append(key.hashCode(), value);
    }

    public void remove(Object key) {
        mArray.remove(key.hashCode());
    }

    public void clear() {
        mArray.clear();
    }

    public V get(Object key) {
        return mArray.get(key.hashCode());
    }

    public boolean containsKey(Object key) {
        return mArray.indexOfKey(key.hashCode()) >= 0;
    }

    // SparseArray Spec API

    public V valueAt(int index) {
        return mArray.valueAt(index);
    }

    public int indexOfKey(Object key) {
        return mArray.indexOfKey(key.hashCode());
    }
}