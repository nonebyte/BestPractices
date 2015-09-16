package com.bestpractices.base.collection;

import android.util.SparseArray;

import com.bestpractices.base.collection.LinkedPool.InstanceCreator;

public class Params implements LinkedPool.ILinkedPoolable {
    // Pool Logic
    private static final int MAX_RECYCLED = 16;
    private static final ThreadLocal<LinkedPool<Params>> sPool = new ThreadLocal<LinkedPool<Params>>();

    private static LinkedPool<Params> getPool() {
        if (sPool.get() == null) {
            sPool.set(new LinkedPool<Params>(CREATOR, MAX_RECYCLED));
        }
        return sPool.get();
    }

    private static final InstanceCreator<Params> CREATOR = new InstanceCreator<Params>() {
        public Params createInstance() {
            return new Params();
        }
    };

    // Static API

    /**
     * Get with default value when params is null or key is not contained.
     *
     * @param params
     * @param key
     * @param defaultValue
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Params params, int key, T defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        return (T) params.get(key);
    }

    /**
     * Safe way to check if a params which is possibly {@code null} contains a
     * key.
     *
     * @param params
     * @param key
     * @return
     */
    public static boolean contains(Params params, int key) {
        return params != null ? params.containsKey(key) : false;
    }

    /**
     * Safe way to recycle a params if it's not null.
     *
     * @param params
     */
    public static void recycle(Params params) {
        if (params != null) {
            params.recycle();
        }
    }

    // Instance API

    /**
     * Ensure {@link #obtain()} and {@link #recycle()} as a pair of calls.
     */
    public static Params obtain() {
        return getPool().obtain();
    }

    /**
     * Convenient method to obtain {@link #Params} with one param only.
     */
    public static Params obtain(int key, Object value) {
        return getPool().obtain().put(key, value);
    }

    public static Params obtain(Params src) {
        return obtain().merge(src);
    }

    public final void recycle() {
        mMap.clear();
        getPool().recycle(this);
    }

    /**
     * Create an instance out of pool management.
     */
    public static Params create() {
        return new Params();
    }

    /**
     * Convenient method to create {@link #Params} with one param only.
     */
    public static Params create(int key, Object value) {
        return new Params().put(key, value);
    }

    public <T> T get(int key) {
        return get(key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int key, T valueIfKeyNotFound) {
        ensureNotRecycled();
        Object value = mMap.get(key, valueIfKeyNotFound);
        return (T) value;
    }

    public boolean containsKey(int key) {
        ensureNotRecycled();
        return mMap.indexOfKey(key) >= 0;
    }

    public boolean isEmpty() {
        return mMap.size() == 0;
    }

    public Params merge(Params src) {
        /** For {@link SparseArray}, ascending append is more efficient */
        for (int i = 0, size = src.size(); i < size; ++i) {
            mMap.append(src.keyAt(i), src.valueAt(i));
        }
        return this;
    }

    public Params clone() {
        return new Params().merge(this);
    }

    // SparseArray Methods

    public int size() {
        return mMap.size();
    }

    public int indexOfKey(int key) {
        return mMap.indexOfKey(key);
    }

    public int indexOfValue(Object value) {
        return mMap.indexOfValue(value);
    }

    public int keyAt(int index) {
        return mMap.keyAt(index);
    }

    public Object valueAt(int index) {
        return mMap.valueAt(index);
    }

    public Params put(int key, Object value) {
        ensureNotRecycled();
        mMap.append(key, value);
        return this;
    }

    public Params remove(int key) {
        ensureNotRecycled();
        mMap.remove(key);
        return this;
    }

    public Params clear() {
        ensureNotRecycled();
        mMap.clear();
        return this;
    }

    // Internal

    private final SparseArray<Object> mMap = new SparseArray<Object>();

    private Params() {
    }

    private void ensureNotRecycled() {
        getPool().ensureNotRecycled(this);
    }

    // Implement ILinkedPoolable

    private Object mNext = null;

    @Override
    public Object getNext() {
        return mNext;
    }

    @Override
    public void setNext(Object nextNode) {
        mNext = nextNode;
    }

    @Override
    public String toString() {
        if (size() <= 0) {
            return "{}";
        }

        StringBuilder buffer = new StringBuilder(size() * 28);
        buffer.append('{');
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keyAt(i);
            buffer.append(key);
            buffer.append('=');
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

}