package com.coodays.pushservicelib.utils;

import android.os.Build;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 判断系统是不是miui，flyme，emui
 */
public class CdOSUtils {
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static boolean isPropertiesExist(String... keys) {
        try {
            CdBuildProperties prop = CdBuildProperties.newInstance();
            for (String key : keys) {
                String str = prop.getProperty(key);
                if (str == null) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isEMUI() {
        return isPropertiesExist(KEY_EMUI_VERSION_CODE);
    }

    public static boolean isMIUI() {
        return isPropertiesExist(KEY_MIUI_VERSION_CODE, KEY_MIUI_VERSION_NAME);
    }

    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    private String getEmuiVersion()
    {
        String buildVersion = "";
        int ver = 0;
        try
        {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[] {String.class});
            buildVersion = (String)getMethod.invoke(classType, new Object[] {"ro.build.version.emui"});
        }
        catch (Exception e)
        {

            return "";
        }

        return buildVersion;
    }
}