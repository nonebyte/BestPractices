package com.bestpractices.base.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bestpractices.base.BaseConfig;
import com.bestpractices.base.ContextManager;


public class PrefUtils {
    public static SharedPreferences pref() {
        String name = AppHelper.packageName() + "_preferences";
        return pref(name);
    }

    public static SharedPreferences pref(String name) {
        int mode = Context.MODE_PRIVATE;
        if (BaseConfig.SDK >= 11) {
            // After sdk 11, this flag must be set to make share preference consistent in multi-process use.
            // One example is using share preference in BroadcastReceiver. BroadcastReceiver.onEvent mostly called
            // in other process.
            mode |= Context.MODE_MULTI_PROCESS;
        }

        return ContextManager.appContext().getSharedPreferences(name, mode);
    }

    public static void put(String prefKey, int val) {
        pref().edit().putInt(prefKey, val).commit();
    }

    public static void put(String name, String prefKey, int val) {
        pref(name).edit().putInt(prefKey, val).commit();
    }

    public static void put(String prefKey, long val) {
        pref().edit().putLong(prefKey, val).commit();
    }

    public static void put(String name, String prefKey, long val) {
        pref(name).edit().putLong(prefKey, val).commit();
    }

    public static void put(String prefKey, boolean val) {
        pref().edit().putBoolean(prefKey, val).commit();
    }

    public static void put(String name, String prefKey, boolean val) {
        pref(name).edit().putBoolean(prefKey, val).commit();
    }

    public static void put(String prefKey, String val) {
        pref().edit().putString(prefKey, val).commit();
    }

    public static void put(String name, String prefKey, String val) {
        pref(name).edit().putString(prefKey, val).commit();
    }

    public static int getInt(String prefKey, int defVal) {
        return pref().getInt(prefKey, defVal);
    }

    public static int getInt(String name, String prefKey, int defVal) {
        return pref(name).getInt(prefKey, defVal);
    }

    public static long getLong(String prefKey, long defVal) {
        return pref().getLong(prefKey, defVal);
    }

    public static long getLong(String name, String prefKey, long defVal) {
        return pref(name).getLong(prefKey, defVal);
    }

    public static boolean getBoolean(String prefKey, boolean defVal) {
        return pref().getBoolean(prefKey, defVal);
    }

    public static boolean getBoolean(String name, String prefKey, boolean defVal) {
        return pref(name).getBoolean(prefKey, defVal);
    }

    public static String getString(String prefKey, String defVal) {
        return pref().getString(prefKey, defVal);
    }

    public static String getString(String name, String prefKey, String defVal) {
        return pref(name).getString(prefKey, defVal);
    }

    public static void increaseIfExist(String prefKey) {
        int val = getInt(prefKey, -1);
        if (val != -1) {
            put(prefKey, val + 1);
        }
    }

    public static void increaseIfExist(String name, String prefKey) {
        int val = getInt(name, prefKey, -1);
        if (val != -1) {
            put(name, prefKey, val + 1);
        }
    }

    public static void increase(String prefKey) {
        put(prefKey, getInt(prefKey, 0) + 1);
    }

    public static void increase(String name, String prefKey) {
        put(name, prefKey, getInt(name, prefKey, 0) + 1);
    }

    public static boolean contains(String name, String prefKey) {
        return pref(name).contains(prefKey);
    }
}
