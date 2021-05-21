package com.runyuanj.register;

import com.runyuanj.util.TransformeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 如何确保及时清除register的props
 */
public class ActionRegister {

    private Map<String, Supplier> map;

    private Map<String, Function> functions;

    private PayloadsWrapper payloads;

    public ActionRegister() {
        this.map = new HashMap<>();
        this.functions = new HashMap<>();
        this.payloads = new PayloadsWrapper();
    }

    public <T> void register(String action, Supplier<T> supplier) {
        if (map.get(action) != null) {
            throw new RuntimeException("Duplicate action name!");
        }
        map.put(action, supplier);
    }

    public <T, R> void registerFunctions(String action, Function<T, R> function) {
        if (map.get(action) != null) {
            throw new RuntimeException("Duplicate action name!");
        }
        functions.put(action, function);
    }

    public <T> Supplier<T> getSupplier(String action) {
        return map.get(action);
    }

    public <T, R> Function<T, R> getFunction(String action) {
        return functions.get(action);
    }

    public <T> T take(String name, Integer index, Class<T> type) {
        return payloads.take(name, index, type);
    }

    public PayloadsWrapper getPayloads() {
        return this.payloads;
    }

//    public <T> T param(String action, PayloadsWrapper payloads, Integer index, Class<T> type) {
//        Object[] objects = payloads.getActionProps(action);
//        if (objects != null && objects.length > index) {
//            return TransformeUtil.convert(objects[index], type);
//        } else {
//            throw new IndexOutOfBoundsException("Action " + action + " param index out of range!");
//        }
//    }

//    public void setActionProps(String action, Object ...objects) {
//        this.payloads.put(action, objects);
//    }

}
