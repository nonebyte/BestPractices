package com.bestpractices.base.collection;

import android.util.SparseArray;

public class SlimHashSet
{
    private final SparseArray<Object> mSparseArray;

    public SlimHashSet()
    {
        this(10);
    }
    
    public SlimHashSet(int capacity)
    {
        mSparseArray = new SparseArray<Object>(capacity);
    }

    public void add(Object object)
    {
        mSparseArray.append(object.hashCode(), object);
    }

    public boolean remove(Object object)
    {
        int key = object.hashCode();
        int index = mSparseArray.indexOfKey(key);
        if (index >= 0)
        {
            mSparseArray.removeAt(index);
            return true;
        }
        return false;
    }

    public boolean contains(Object object)
    {
        return mSparseArray.indexOfKey(object.hashCode()) >= 0;
    }

    public void clear()
    {
        mSparseArray.clear();
    }

    public boolean isEmpty()
    {
        return mSparseArray.size() > 0;
    }

    public int size()
    {
        return mSparseArray.size();
    }
}