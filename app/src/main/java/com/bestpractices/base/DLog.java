package com.bestpractices.base;

import android.util.Log;
import android.widget.Toast;

public class DLog {
    public static void i(String tag, String msg, Object... args) {
        if (BaseConfig.DEBUG) {
            Log.i(tag, format(msg, args));
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (BaseConfig.DEBUG) {
            Log.d(tag, format(msg, args));
        }
    }

    public static void w(String tag, String msg, Object... args) {
        if (BaseConfig.DEBUG) {
            Log.w(tag, format(msg, args));
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (BaseConfig.DEBUG) {
            Log.e(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg, Object... args) {
        if (BaseConfig.DEBUG) {
            Log.e(tag, format(msg, args));
        }
    }

    public static void v(String tag, String msg, Object... args) {
        if (BaseConfig.DEBUG) {
            Log.v(tag, format(msg, args));
        }
    }

    private static String format(String msg, Object[] args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        return msg;
    }


    public static void toast(String msg) {
        if (BaseConfig.DEBUG) {
            Toast.makeText(ContextManager.appContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}