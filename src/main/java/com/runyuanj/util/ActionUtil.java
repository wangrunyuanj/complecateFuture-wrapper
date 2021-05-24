package com.runyuanj.util;

public class ActionUtil {

    public static final String GET_ACTION_FUTURE = "&sync-";

    public static String getFuture(String name) {
        return GET_ACTION_FUTURE + name;
    }
}
