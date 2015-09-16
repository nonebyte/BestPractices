package com.bestpractices.base.collection;

public class LinkedPool<T extends LinkedPool.ILinkedPoolable> {
    /**
     * We use {@link T#getNext()} != null to check if a params is recycled, so:
     * <p>
     * (1) must ensure {@link T#getNext()} == null when it's obtained
     * </p>
     * <p>
     * (2) must ensure {@link T#getNext()} != null when it's recycled
     * </p>
     * So we need an readonly {@link #EMPTY} to handle some cases where next
     * node is null in {@link #recycle()}.
     */
    private static final Object EMPTY = new Object();

    private final InstanceCreator<T> mCreator;
    private final int mMaxRecycled;

    private int mRecyclerUsed;
    private T mRecyclerTop;

    /**
     * Build a pool for any object implement {@link ILinkedPoolable}
     *
     * @param creator     Without an instance, we have no way to get runtime info of
     *                    generic type in Java, so we must set a thing to tell
     *                    {@link #LinkedPool} how to create an instance. A creator is
     *                    much more efficient than Class<T>.
     * @param maxRecycled
     */
    public LinkedPool(InstanceCreator<T> creator, int maxRecycled) {
        mCreator = creator;
        mMaxRecycled = maxRecycled;
    }

    @SuppressWarnings("unchecked")
    public T obtain() {
        final T node = mRecyclerTop;
        if (node == null) {
            return mCreator.createInstance();
        }

        Object nextNode = node.getNext();
        node.setNext(null);

        mRecyclerTop = nextNode != EMPTY ? (T) nextNode : null;
        --mRecyclerUsed;

        return node;
    }

    public void recycle(T node) {
        ensureNotRecycled(node);

        Object nextNode = null;
        if (mRecyclerUsed < mMaxRecycled) {
            ++mRecyclerUsed;
            nextNode = mRecyclerTop;
            mRecyclerTop = node;
        }
        if (nextNode == null) {
            nextNode = EMPTY;
        }
        node.setNext(nextNode);
    }

    public void ensureNotRecycled(T node) {
        if (node.getNext() != null) {
            throw new RuntimeException(node + " is recycled");
        }
    }

    public static interface ILinkedPoolable {
        public Object getNext();

        public void setNext(Object nextNode);
    }

    public static interface InstanceCreator<T> {
        public T createInstance();
    }
}