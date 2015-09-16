package com.bestpractices.base.util;

import com.bestpractices.base.Assert;

import java.util.ArrayList;

public final class TextHelper {
    public static final String EMPTY = "";

    // Common

    /**
     * Check if a string is empty or pure spaces only
     */
    public static boolean isEmptyOrSpaces(String value) {
        if (value != null) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (value.charAt(i) != ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ensure null string will be converted as ""
     */
    public static String ensureNotNull(String s) {
        return s == null ? "" : s;
    }

    // Multiline

    public static final String LINE_BREAK = System.getProperty("line.separator");

    public static String lines(String... lines) {
        StringBuilder sb = new StringBuilder();
        int end = lines.length - 1;
        for (int i = 0; i < end; ++i) {
            sb.append(lines[i]).append(LINE_BREAK);
        }
        sb.append(lines[end]);
        return sb.toString();
    }

    // Concat

    public static final String concat(String... objs) {
        Assert.r(objs.length > 1 && objs[0] != null);
        StringBuilder sb = new StringBuilder();
        return concat(sb, objs);
    }

    public static final String concat(StringBuilder sb, String... objs) {
        Assert.r(sb != null);
        for (int i = 0, len = objs.length; i < len; ++i) {
            if (objs[i] != null) {
                sb.append(objs[i]);
            }
        }
        return sb.toString();
    }

    // Split

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static String[] split(String src, String splitter) {
        return split(src, splitter, true, false);
    }

    /**
     * Split string and make sure the result is predictable.
     * <p/>
     * <p>
     * For example, String.split() will return ["","","a"] for "``a", but ["a"]
     * for "a``" which is unpredictable.
     * </p>
     *
     * @param splitter         currently doesn't support regular expression
     * @param keepEmpty
     * @param pureSpaceIsEmpty
     * @return
     */
    public static String[] split(String src, String splitter, boolean keepEmpty, boolean pureSpaceIsEmpty) {
        final int srcLen = src.length();
        if (src == null || srcLen == 0) {
            return EMPTY_STRING_ARRAY;
        }

        int index;
        int lastIndex = 0;
        String subStr;
        ArrayList<String> tmpResult = new ArrayList<String>();

        while (lastIndex <= srcLen) {
            // when lastIndex == srcLen, no exception, index = -1
            index = src.indexOf(splitter, lastIndex);
            if (index < 0) {
                index = srcLen;
            }
            subStr = src.substring(lastIndex, index);
            lastIndex = index + 1;
            if (pureSpaceIsEmpty && isEmptyOrSpaces(subStr)) {
                subStr = "";
            }
            if (keepEmpty || subStr.length() > 0) {
                tmpResult.add(subStr);
            }
        }

        String[] result = new String[tmpResult.size()];
        tmpResult.toArray(result);
        return result;
    }

}