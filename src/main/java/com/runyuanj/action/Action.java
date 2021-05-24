package com.runyuanj.action;

/**
 * 需要包含namespace
 */
public interface Action {

    String getName();

    Object getAction();

    Object[] getProps();

    void setProps(Object... props);
}
