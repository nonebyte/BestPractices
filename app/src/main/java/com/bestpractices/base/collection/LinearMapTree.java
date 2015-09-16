package com.bestpractices.base.collection;

import com.bestpractices.base.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;


@SuppressWarnings("unchecked")
public abstract class LinearMapTree<E extends LinearMapTree<E>> extends LinkedTree<E> {
    // Linear Map

    public static final int INVALID_POSITION = -1;

    protected IntArrayList mPosList;

    public final E get(int position) {
        // 1) exclude self node
        ++position;
        Assert.r(position > 0 || position < nodeCount());

        // 2) search
        E curr = (E) this;
        IntArrayList list = mPosList;
        int index;
        while (curr != null) {
            index = list.binarySearch(position);
            if (index >= 0) {
                return curr.mChildren.get(index);
            } else {
                index = -index - 2;
                position -= list.get(index);
                curr = curr.mChildren.get(index);
                list = curr.mPosList;
            }
        }
        return null;
    }

    @Override
    protected ArrayList<E> ensureChildren() {
        if (mChildren == null) {
            mPosList = new IntArrayList();
        }
        return super.ensureChildren();
    }

    // Size

    public int size() {
        return nodeCount() - 1;
    }

    int mActualNodeCount = 1;

    /**
     * The total count of nodes of this sub-tree. Return 1 if this node is
     * folded.
     */
    protected int nodeCount() {
        return mIsFolded ? 1 : mActualNodeCount;
    }

    // Fold

    /**
     * If folded, nodeCount() always return 1
     */
    private boolean mIsFolded = false;

    public void fold(boolean toFold) {
        if (mIsFolded != toFold) {
            // 1) update tree if needed
            if (mParent != null) {
                int change = mActualNodeCount - 1;
                updateTreeNodesInfo(mParent, mParent.mChildren.indexOf(this), toFold ? -change : change);
            }

            // 2) ensure unfolded before notify change
            mIsFolded = false;
            onLinearChanged(toFold ? CHANGE_TYPE_FOLD : CHANGE_TYPE_UNFOLD, 1, mActualNodeCount - 1);
            mIsFolded = toFold;
        }
    }

    public final boolean isFolded() {
        return mIsFolded;
    }

    // Add

    @Override
    public E add(int index, E child) {
        E thisNode = super.add(index, child);

        int localPos = index == 0 ? 1 : (mPosList.get(index - 1) + mChildren.get(index - 1).nodeCount());
        mPosList.add(index, localPos);
        updateTreeNodesInfo(thisNode, index, child.nodeCount());

        onLinearChanged(CHANGE_TYPE_ADD, localPos, child.nodeCount());
        return thisNode;
    }

    @Override
    public E add(int index, Collection<E> collection) {
        E thisNode = super.add(index, collection);

        int addedNodeCount = 0;
        int i = index;
        int localPos = index == 0 ? 1 : (mPosList.get(index - 1) + mChildren.get(index - 1).nodeCount());
        int pos = localPos;
        for (E child : collection) {
            mPosList.add(i++, pos);
            addedNodeCount += child.nodeCount();
            pos += child.nodeCount();
        }
        updateTreeNodesInfo(thisNode, index + collection.size() - 1, addedNodeCount);

        onLinearChanged(CHANGE_TYPE_ADD, localPos, addedNodeCount);
        return thisNode;
    }

    // Remove

    @Override
    public void remove(int startIndex, int endIndex) {
        final ArrayList<E> children = mChildren;
        final int localPos = mPosList.get(startIndex);
        final int removedNodeCount = mPosList.get(endIndex) + children.get(endIndex).nodeCount() - localPos;

        updateTreeNodesInfo((E) this, endIndex, -removedNodeCount);
        mPosList.removeRange(startIndex, endIndex + 1);

        super.remove(startIndex, endIndex);

        onLinearChanged(CHANGE_TYPE_REMOVE, localPos, removedNodeCount);
    }

    // Set

    @Override
    public void set(int index, E child) {
        E oldChild = childAt(index);
        if (oldChild != child) {
            super.set(index, child);

            int nodeCountChanged = child.nodeCount();
            if (oldChild != null) {
                nodeCountChanged -= oldChild.nodeCount();
            }
            updateTreeNodesInfo((E) this, index, nodeCountChanged);

            onLinearChanged(CHANGE_TYPE_REPLACE, mPosList.get(index), oldChild.nodeCount(), child.nodeCount());
        }
    }

    // Move

    @Override
    public void move(E fromParent, int fromStartIndex, int fromEndIndex, E toParent, int toStartIndex) {
        if (fromStartIndex > fromEndIndex) {
            return;
        }
        if (fromParent == toParent && fromStartIndex == toStartIndex) {
            return;
        }

        // 1) find Loweast Common Ancestor(LCA)
        E lca = fromParent;
        if (fromParent != toParent) {
            lca = null;
            SlimHashSet fromPath = new SlimHashSet();
            SlimHashSet toPath = new SlimHashSet();
            fromPath.add(fromParent);
            toPath.add(toParent);
            E fromCurr = fromParent;
            E toCurr = toParent;
            while (fromCurr.mParent != null || toCurr.mParent != null) {
                if (fromCurr.mParent != null) {
                    fromCurr = fromCurr.mParent;
                    if (toPath.contains(fromCurr)) {
                        lca = fromCurr;
                        break;
                    }
                    fromPath.add(fromCurr);
                }

                if (toCurr.mParent != null) {
                    toCurr = toCurr.mParent;
                    if (fromPath.contains(toCurr)) {
                        lca = toCurr;
                        break;
                    }
                    toPath.add(toCurr);
                }
            }
        }
        if (lca == null) {
            Assert.d(false);
            return;
        }

        // 2) record position info
        E thisNode = (E) this;
        IntArrayList list = fromParent.mPosList;
        int nodeCountToMove = list.get(fromEndIndex) + fromParent.mChildren.get(fromEndIndex).nodeCount() - list.get(fromStartIndex);
        int fromBasePos = findPosition(thisNode, fromParent, 0);
        int toBasePos = findPosition(thisNode, toParent, 0);
        int fromStartPos = INVALID_POSITION;
        int fromEndPos = INVALID_POSITION;
        int toStartPos = INVALID_POSITION;
        int toEndPos = INVALID_POSITION;
        if (fromBasePos != INVALID_POSITION) {
            fromStartPos = fromBasePos + fromParent.mPosList.get(fromStartIndex);
            fromEndPos = fromStartPos + nodeCountToMove - 1;
        }
        if (toBasePos != INVALID_POSITION) {
            toStartPos = toBasePos + (toStartIndex == 0 ? 1 : toParent.mPosList.get(toStartIndex - 1) + toParent.mChildren.get(toStartIndex - 1).nodeCount());
            toEndPos = toStartPos + nodeCountToMove - 1;
        }

        // 3) update tree info until LCA
        updateTreeNodesInfo(fromParent, lca.mParent, fromEndIndex, -nodeCountToMove);
        updateTreeNodesInfo(toParent, lca.mParent, toStartIndex - 1, nodeCountToMove);

        // 4) update to parent pos list
        fromParent.mPosList.removeRange(fromStartIndex, fromEndIndex + 1);

        // 5) update from parent pos list
        int startIndex = toStartIndex;
        if (fromParent == toParent && fromStartIndex < toStartIndex) {
            startIndex -= fromEndIndex - fromStartIndex + 1;
        }
        list = toParent.mPosList;
        int pos = startIndex == 0 ? 1 : (list.get(startIndex - 1) + toParent.mChildren.get(startIndex - 1).nodeCount());
        ArrayList<E> children = fromParent.mChildren;
        for (int i = fromStartIndex, j = startIndex; i <= fromEndIndex; ++i, ++j) {
            list.add(j, pos);
            pos += children.get(i).nodeCount();
        }

        // 6) do move
        super.move(fromParent, fromStartIndex, fromEndIndex, toParent, toStartIndex);

        // 7) notify
        onLinearChanged(CHANGE_TYPE_MOVE, fromStartPos, fromEndPos, toStartPos, toEndPos);
    }

    private static <E extends LinearMapTree<E>> int findPosition(E root, E parent, int localPos) {
        while (true) {
            if (parent.isFolded()) {
                return INVALID_POSITION;
            }

            if (parent.mParent == null) {
                break;
            }
            localPos += parent.mParent.mPosList.get(parent.mParent.mChildren.indexOf(parent));
            parent = parent.mParent;
        }
        return localPos;
    }

    // Clear

    @Override
    public void clear() {
        super.clear();

        if (mPosList != null && !mPosList.isEmpty()) {
            mPosList.clear();

            int removedNodeCount = nodeCount() - 1;
            if (removedNodeCount > 0) {
                updateTreeNodesInfo((E) this, -1, -removedNodeCount);
                onLinearChanged(CHANGE_TYPE_REMOVE, 1, removedNodeCount);
            } else {
                mActualNodeCount = 1;
            }
        }
    }

    // Sort

    @Override
    public void sort(Comparator<E> cmp) {
        super.sort(cmp);
        rebuildPosList();
        onLinearChanged(CHANGE_TYPE_INVALIDATE);
    }

    private void rebuildPosList() {
        if (mPosList != null && !mPosList.isEmpty()) {
            int pos = 1;
            for (int i = 0, end = mChildren.size(); i < end; ++i) {
                mPosList.set(i, pos);
                pos += mChildren.get(i).nodeCount();
            }
        }
    }

    // Common

    private static <E extends LinearMapTree<E>> void updateTreeNodesInfo(E parent, int childIndex, int change) {
        updateTreeNodesInfo(parent, null, childIndex, change);
    }

    private static <E extends LinearMapTree<E>> void updateTreeNodesInfo(E parent, E nodeCountEffectBound, int childIndex, int change) {
        if (change == 0) {
            return;
        }

        IntArrayList list;
        while (parent != null && parent != nodeCountEffectBound) {
            // 1) update pos list
            list = parent.mPosList;
            for (int i = list.size() - 1; i > childIndex; --i) {
                list.offset(i, change);
            }

            // 2) update node count
            parent.mActualNodeCount += change;

            // 3) check folded to stop
            if (parent.isFolded()) {
                return;
            }

            // 4) go up
            if (parent.mParent != null) {
                childIndex = parent.mParent.mChildren.indexOf(parent);
            }
            parent = parent.mParent;
        }
    }

    // Overridable

    /**
     * args: [startPos, count]
     */
    protected static final int CHANGE_TYPE_ADD = 1;
    protected static final int CHANGE_TYPE_REMOVE = 2;
    protected static final int CHANGE_TYPE_FOLD = 3;
    protected static final int CHANGE_TYPE_UNFOLD = 4;
    protected static final int CHANGE_TYPE_REPLACE = 5;
    protected static final int CHANGE_TYPE_MOVE = 6;
    protected static final int CHANGE_TYPE_INVALIDATE = 7;

    protected void onLinearChanged(int type, int... args) {
    }
}