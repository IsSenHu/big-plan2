package com.gapache.job.common.utils;

import java.util.regex.Pattern;

/**
 * @author HuSen
 * @since 2021/2/4 9:36 上午
 */
public class OsUtils {

    public static boolean isWin(String osName) {
        return Pattern.matches("Windows.*", osName);
    }

    public static boolean isMac(String osName) {
        return Pattern.matches("Mac.*", osName);
    }

    public static boolean isLinux(String osName) {
        return Pattern.matches("Linux.*", osName);
    }

    public static String osName() {
        return System.getProperty("os.name");
    }
}
