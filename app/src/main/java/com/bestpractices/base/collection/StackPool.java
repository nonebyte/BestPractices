package com.bestpractices.base.collection;

import com.bestpractices.base.Check;

import java.util.ArrayList;

public class StackPool<T> {
    private static final int DEFAULT_CAPACITY = 8;
    private static final int DEFAULT_MAX_RECYCLE = 16;

    private static final ItemAdapter<Object> EMPTY_ADAPTER = new ItemAdapter<Object>() {
    };

    /**
     * {@link #ArrayList<T>} is more efficient than {@link #Stack<T>} because
     * it's not thread safe.
     */
    private final ArrayList<T> mRecycledStack;
    private final ItemAdapter<T> mAdapter;

    /**
     * Pending stack will hold items temporarily before recycled to quickly
     * resume when obtain avoiding calling {@link ItemAdapter#onRecycle(Object)}
     */
    private ArrayList<T> mPendingStack;

    private int mMaxRecycled;

    @SuppressWarnings("unchecked")
    public StackPool() {
        this((ItemAdapter<T>) EMPTY_ADAPTER);
    }

    public StackPool(ItemAdapter<T> adapter) {
        this(DEFAULT_MAX_RECYCLE, adapter);
    }

    public StackPool(int maxRecycled, ItemAdapter<T> adapter) {
        this(maxRecycled, DEFAULT_CAPACITY, adapter);
    }

    /**
     * Create stack pool with detailed config.
     *
     * @param maxRecycled
     * @param capacity
     * @param adapter
     */
    public StackPool(int maxRecycled, int capacity, ItemAdapter<T> adapter) {
        Check.r(maxRecycled >= 0);
        Check.r(capacity >= 0);
        Check.r(adapter != null);

        mMaxRecycled = maxRecycled;
        mRecycledStack = new ArrayList<T>(Math.min(capacity, maxRecycled));
        mAdapter = adapter;
    }

    public void setMaxRecycled(int max) {
        Check.r(max >= 0);

        mMaxRecycled = max;
        final ArrayList<T> stack = mRecycledStack;
        if (stack.size() > max) {
            T item;
            for (int i = stack.size() - 1; i >= max; --i) {
                item = stack.remove(i);
                onItemRecycle(item);
                onItemDestroy(item);
            }
        }
    }

    public boolean isEmpty() {
        return mRecycledStack.isEmpty() && (mPendingStack == null || mPendingStack.isEmpty());
    }

    public int size() {
        return mRecycledStack.size() + (mPendingStack == null ? 0 : mPendingStack.size());
    }

    public T obtain() {
        T item = null;

        boolean callOnObtain = true;
        if (mPendingStack != null && !mPendingStack.isEmpty()) {
            callOnObtain = false;
            item = mPendingStack.remove(mPendingStack.size() - 1);
        } else if (!mRecycledStack.isEmpty()) {
            item = mRecycledStack.remove(mRecycledStack.size() - 1);
        } else {
            item = onItemCreate();
        }

        if (callOnObtain && item != null) {
            onItemObtain(item);
        }
        return item;
    }

    public void recycle(T item) {
        recycle(item, false);
    }

    public void recycle(T item, boolean pending) {
        if (item == null) {
            Check.d(false);
            return;
        }

        if (pending) {
            if (mPendingStack == null) {
                mPendingStack = new ArrayList<T>();
            }
            mPendingStack.add(item);
        } else {
            if (!mRecycledStack.contains(item)) {
                onItemRecycle(item);

                if (mRecycledStack.size() < mMaxRecycled) {
                    mRecycledStack.add(item);
                } else {
                    onItemDestroy(item);
                }
            } else {
                Check.d(false, item + " is repeatly recycled!");
            }
        }
    }

    public void recyclePendings() {
        ArrayList<T> stack = mPendingStack;
        if (stack != null && !stack.isEmpty()) {
            for (T item : stack) {
                recycle(item, false);
            }
            stack.clear();
        }
    }

    public void destroy() {
        // 1) clear pendings
        ArrayList<T> stack = mPendingStack;
        if (stack != null && !stack.isEmpty()) {
            for (T item : stack) {
                onItemRecycle(item);
                onItemDestroy(item);
            }
            stack.clear();
        }

        // 2) clear recycled
        stack = mRecycledStack;
        if (!stack.isEmpty()) {
            for (T item : stack) {
                onItemDestroy(item);
            }
            stack.clear();
        }
    }

    public ArrayList<T> values() {
        return mRecycledStack;
    }

    // Overridable

    protected T onItemCreate() {
        return mAdapter.create();
    }

    protected void onItemObtain(T item) {
        mAdapter.onObtain(item);
    }

    protected void onItemRecycle(T item) {
        mAdapter.onRecycle(item);
    }

    protected void onItemDestroy(T item) {
        mAdapter.destroy(item);
    }

    // Adapter

    public static abstract class ItemAdapter<T> {
        public T create() {
            return null;
        }

        public void destroy(T item) {
        }

        public void onObtain(T item) {
        }

        public void onRecycle(T item) {
        }
    }
}