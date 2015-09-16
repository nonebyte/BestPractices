package com.bestpractices.base.util;

import android.util.SparseBooleanArray;

import com.bestpractices.base.BaseConfig;

import java.util.regex.Pattern;

public class CompatHelper {
    // SDK

    public static boolean sdk(int min) {
        return BaseConfig.SDK >= min;
    }

    public static boolean sdk(int min, int max) {
        return MathHelper.inRange(BaseConfig.SDK, min, max);
    }

    public static boolean sdks(int... targets) {
        int sdk = BaseConfig.SDK;
        for (int target : targets) {
            if (sdk == target) {
                return true;
            }
        }
        return false;
    }

    // Device Name

    private static SparseBooleanArray sDeviceResultCache;

    public static boolean device(String regex) {
        if (sDeviceResultCache == null) {
            sDeviceResultCache = new SparseBooleanArray();
        }

        int hashCode = regex.hashCode();
        int index = sDeviceResultCache.indexOfKey(hashCode);
        boolean result;
        if (index < 0) {
            result = Pattern.matches(regex, SysInfoHelper.device());
            sDeviceResultCache.put(hashCode, result);
        } else {
            result = sDeviceResultCache.valueAt(index);
        }
        return result;
    }

    // CPU Arch

    private static SparseBooleanArray sCPUResultCache;

    public static boolean cpu(String regex) {
        if (sCPUResultCache == null) {
            sCPUResultCache = new SparseBooleanArray();
        }

        int hashCode = regex.hashCode();
        int index = sCPUResultCache.indexOfKey(hashCode);
        boolean result;
        if (index < 0) {
            result = Pattern.matches(regex, SysInfoHelper.cpuArch());
            sCPUResultCache.put(hashCode, result);
        } else {
            result = sCPUResultCache.valueAt(index);
        }
        return result;
    }

    // Ram Size

    public static boolean ram(int min, int max) {
        return MathHelper.inRange(SysInfoHelper.ramSize(), min, max);
    }
}