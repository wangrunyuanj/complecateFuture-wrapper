package com.runyuanj.register;

import com.runyuanj.util.TransformeUtil;

import java.util.HashMap;
import java.util.Map;

public class PayloadsWrapper {

    private Map<String, Object[]> payloads;

    public PayloadsWrapper() {
        this.payloads = new HashMap<>();
    }

    public PayloadsWrapper(int initialCapacity) {
        this.payloads = new HashMap<>(initialCapacity);
    }

    public <T> T take(String name, Integer index, Class<T> type) {
        Object[] objects = this.payloads.get(name);
        if (objects != null && objects.length > index) {
            return TransformeUtil.convert(objects[index], type);
        } else {
            throw new IndexOutOfBoundsException("Action " + name + " param index out of range!");
        }
    }

    public void setActionProps(String action, Object ...immutableProps) {
        this.payloads.put(action, immutableProps);
    }

    public Object[] getActionProps(String action) {
        return this.payloads.get(action);
    }

    public void destroy() {
        this.payloads.clear();
    }

}
