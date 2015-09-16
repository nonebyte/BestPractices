package com.bestpractices.base.collection;

import com.bestpractices.base.Check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("unchecked")
public abstract class LinkedTree<E extends LinkedTree<E>> {
    // Linked

    protected E mParent;
    protected ArrayList<E> mChildren;

    public final E parent() {
        return mParent;
    }

    public final int childCount() {
        return mChildren != null ? mChildren.size() : 0;
    }

    public final E childAt(int index) {
        return ensureChildren().get(index);
    }

    protected ArrayList<E> ensureChildren() {
        if (mChildren == null) {
            mChildren = new ArrayList<E>();
        }
        return mChildren;
    }

    // Add

    public final E add(E child) {
        add(ensureChildren().size(), child);
        return (E) this;
    }

    public final E add(Collection<E> collection) {
        add(ensureChildren().size(), collection);
        return (E) this;
    }

    public E add(int index, E child) {
        Check.r(child != null);

        E thisNode = (E) this;
        ensureChildren().add(index, child);
        child.mParent = thisNode;
        return thisNode;
    }

    public E add(int index, Collection<E> collection) {
        E thisNode = (E) this;
        ensureChildren().addAll(index, collection);
        for (E child : collection) {
            Check.r(child != null);
            child.mParent = thisNode;
        }

        return thisNode;
    }

    // Remove

    public final void remove(E child) {
        int index = ensureChildren().indexOf(child);
        if (index >= 0) {
            remove(index);
        }
    }

    public final void remove(int index) {
        remove(index, index);
    }

    public void remove(int startIndex, int endIndex) {
        ArrayList<E> children = mChildren;
        for (int i = endIndex; i >= startIndex; --i) {
            children.remove(i).mParent = null;
        }
    }

    // Move

    public final void move(int fromIndex, int toIndex) {
        E thisNode = (E) this;
        move(thisNode, fromIndex, fromIndex, thisNode, toIndex);
    }

    public final void move(int fromStartIndex, int fromEndIndex, int toStartIndex) {
        E thisNode = (E) this;
        move(thisNode, fromStartIndex, fromEndIndex, thisNode, toStartIndex);
    }

    public final void move(E parent, int fromIndex, int toIndex) {
        move(parent, fromIndex, fromIndex, parent, toIndex);
    }

    /**
     * FromParent and ToParent must belong to this sub-tree
     */
    public final void move(E fromParent, int fromIndex, E toParent, int toIndex) {
        move(fromParent, fromIndex, fromIndex, toParent, toIndex);
    }

    /**
     * <li>Move will do add before remove</li><br/>
     * <li>FromParent and ToParent must belong to this sub-tree</li>
     */
    public void move(E fromParent, int fromStartIndex, int fromEndIndex, E toParent, int toIndex) {
        if (fromParent == toParent && fromStartIndex == toIndex) {
            return;
        }

        E child;
        ArrayList<E> fromChildren = fromParent.mChildren;
        ArrayList<E> toChildren = toParent.mChildren;
        if (fromChildren == toChildren && fromStartIndex > toIndex) {
            ArrayList<E> tmp = new ArrayList<E>();
            for (int i = fromStartIndex; i <= fromEndIndex; ++i) {
                child = fromChildren.get(i);
                child.mParent = toParent;
                tmp.add(child);
            }
            CollectionHelper.removeRange(fromParent.mChildren, fromStartIndex, fromEndIndex + 1);
            toChildren.addAll(toIndex, tmp);
        } else {
            for (int i = fromStartIndex, j = toIndex; i <= fromEndIndex; ++i, ++j) {
                child = fromChildren.get(i);
                child.mParent = toParent;
                toChildren.add(j, child);
            }
            CollectionHelper.removeRange(fromChildren, fromStartIndex, fromEndIndex + 1);
        }
    }

    // Set

    public void set(int index, E child) {
        Check.r(child != null);

        E oldChild = childAt(index);
        if (oldChild != child) {
            if (oldChild != null) {
                oldChild.mParent = null;
            }
            mChildren.set(index, child);
            child.mParent = (E) this;
        }
    }

    // Clear

    public void clear() {
        if (!CollectionHelper.isEmpty(mChildren)) {
            for (E node : mChildren) {
                node.mParent = null;
            }
            mChildren.clear();
        }
    }

    // Sort

    public void sort(Comparator<E> cmp) {
        if (!CollectionHelper.isEmpty(mChildren)) {
            Collections.sort(mChildren, cmp);
        }
    }
}