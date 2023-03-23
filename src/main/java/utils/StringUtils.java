package utils;

public class StringUtils {

    public static String[] split(String s, int limit) {
        int slotCount = s.length() / limit + 1;

        String[] ret = new String[slotCount];
        for (int i = 0; i < ret.length; i++) {
            int start = i * limit, end = Math.min(start + limit, s.length());
            ret[i] = s.substring(start, end);
        }

        return ret;
    }
}
