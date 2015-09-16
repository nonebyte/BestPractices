package com.bestpractices.base.collection;

import java.util.ArrayList;
import java.util.List;

public class CollectionHelper {
    // List

    @SafeVarargs
    public static <E> ArrayList<E> arrayList(E... initElements) {
        ArrayList<E> list = new ArrayList<E>();
        for (E elem : initElements) {
            list.add(elem);
        }
        return list;
    }

    /**
     * As for list (a,b,c,d), move(0,1,3) will move (a,b) to the place after c,
     * and c will bubble to fill the opening, resulting in (c,a,b,d).</br> The
     * last parameter is 3 because c is at place 2 at first, so moving (a,b)
     * after c means moving (a,b) to place 3</br> <b>ATTENTION:</b></br>
     * <b>Parameter</b> toStartIndex is based on the status before move, because
     * there is no need to know the final places of elements after move.
     */
    public static <E> void move(ArrayList<E> list, int fromIndex, int toIndex) {
        move(list, fromIndex, fromIndex, toIndex);
    }

    /**
     * As for list (a,b,c,d), move(0,1,3) will move (a,b) to the place after c,
     * and c will bubble to fill the opening, resulting in (c,a,b,d).</br> The
     * last parameter is 3 because c is at place 2 at first, so moving (a,b)
     * after c means moving (a,b) to place 3</br> <b>ATTENTION:</b></br>
     * <b>Parameter</b> toStartIndex is based on the status before move, because
     * there is no need to know the final places of elements after move.
     */
    public static <E> void move(ArrayList<E> list, int fromStartIndex, int fromEndIndex, int toStartIndex) {
        // 1) check out of bounds
        final int size = list.size();
        if (fromStartIndex < 0 || fromStartIndex >= size //
                || fromEndIndex < 0 || fromEndIndex >= size || fromEndIndex < fromStartIndex//
                || toStartIndex < 0 || toStartIndex > size) {
            return;
        }

        // 2) check no move
        if (fromStartIndex == toStartIndex) {
            return;
        }

        // 3) do move
        final int moveItemCount = fromEndIndex - fromStartIndex + 1;
        final ArrayList<E> moveItems = new ArrayList<E>(moveItemCount);
        for (int k = fromStartIndex; k <= fromEndIndex; ++k) {
            moveItems.add(list.get(k));
        }
        if (fromStartIndex < toStartIndex) {
            toStartIndex -= moveItemCount;
            for (int i = fromStartIndex, k = fromEndIndex + 1; i < toStartIndex; ++i, ++k) {
                list.set(i, list.get(k));
            }
        } else {
            for (int i = fromEndIndex, k = fromStartIndex - 1; k >= toStartIndex; --i, --k) {
                list.set(i, list.get(k));
            }
        }
        for (int i = toStartIndex, k = 0, len = moveItems.size(); k < len; ++i, ++k) {
            list.set(i, moveItems.get(k));
        }
    }

    public static <E> void removeRange(ArrayList<E> list, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; --i) {
            list.remove(i);
        }
    }

    public static <E> E last(ArrayList<E> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isEmpty(IntArrayList list) {
        return list == null || list.isEmpty();
    }
}