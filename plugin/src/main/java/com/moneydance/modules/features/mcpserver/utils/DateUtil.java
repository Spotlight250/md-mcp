package com.moneydance.modules.features.mcpserver.utils;

/**
 * Utility for Moneydance dates (int YYYYMMDD).
 */
public class DateUtil {

    /**
     * Converts YYYY-MM-DD string to YYYYMMDD integer.
     */
    public static int decodeIsoDate(String isoDate) {
        if (isoDate == null || isoDate.length() != 10) {
            return 0;
        }
        try {
            String clean = isoDate.replace("-", "");
            return Integer.parseInt(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Converts YYYYMMDD integer to YYYY-MM-DD string.
     */
    public static String encodeIsoDate(int mdDate) {
        if (mdDate <= 0) return "";
        String s = String.valueOf(mdDate);
        if (s.length() != 8) return s;
        return s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8);
    }

    /**
     * Returns today's date in YYYYMMDD format.
     */
    public static int getToday() {
        return Integer.parseInt(new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()));
    }
}
