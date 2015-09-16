/*
 *  Copyright (C) 2005-2015 UCWeb Inc. All rights reserved.
 *  Description :SlimHashMap.java
 *
 *  Creation    : 2015-09-16
 *  Author      : chenjun3@ucweb.com
 */

package com.bestpractices.base.collection;

import android.util.SparseArray;

public class SlimHashMap<V>
{
    private final SparseArray<V> mArray;

    public SlimHashMap()
    {
        this(10);
    }

    public SlimHashMap(int initCapacity)
    {
        mArray = new SparseArray<V>(initCapacity);
    }

    public void put(Object key, V value)
    {
        mArray.append(key.hashCode(), value);
    }

    public void remove(Object key)
    {
        mArray.remove(key.hashCode());
    }

    public void clear()
    {
        mArray.clear();
    }

    public V get(Object key)
    {
        return mArray.get(key.hashCode());
    }

    public boolean containsKey(Object key)
    {
        return mArray.indexOfKey(key.hashCode()) >= 0;
    }

    // SparseArray Spec API

    public V valueAt(int index)
    {
        return mArray.valueAt(index);
    }

    public int indexOfKey(Object key)
    {
        return mArray.indexOfKey(key.hashCode());
    }
}