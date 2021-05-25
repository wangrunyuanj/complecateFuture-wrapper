package com.runyuanj.util;

import com.runyuanj.wrapper.AnyOfActionNameWrapper;
import com.runyuanj.wrapper.FutureNameWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class ActionUtil {

    public static final String GET_ACTION_FUTURE = "&sync-";

    public static FutureNameWrapper getFuture(String name) {
        return new FutureNameWrapper(name);
    }

    public static AnyOfActionNameWrapper getAnyOf(String name) {
        return new AnyOfActionNameWrapper(name);
    }

    public static String validateActionName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("Name can not be empty");
        }
        if (name.startsWith(GET_ACTION_FUTURE)) {
            throw new RuntimeException("Name can not start with " + GET_ACTION_FUTURE);
        }
        return name.trim();
    }
}
