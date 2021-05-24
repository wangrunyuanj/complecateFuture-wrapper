package com.runyuanj.register;


import com.runyuanj.action.Action;
import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 容纳了任务执行所需的环境
 * 包括定义的functions和suppliers,
 * 每一个action执行的Future,
 * 以及最后一个action.
 * 使用close()返回结果, 并清空记录的最后一个action
 */
public class ActionDefinitionContainer {

    private Map<String, SupplierAction> suppliers = new HashMap<>();

    private Map<String, FunctionAction> functions = new HashMap<>();

    private Map<String, CompletableFuture> results = new HashMap<>();

    private Action lastAction;

    public ActionDefinitionContainer addSupplier(SupplierAction... actions) {
        // name不能重复
        Arrays.stream(actions).forEach((action) -> this.suppliers.put(action.getName(), action));
        return this;
    }

    public ActionDefinitionContainer addFunction(FunctionAction... actions) {
        // name不能重复
        Arrays.stream(actions).forEach((action) -> this.functions.put(action.getName(), action));
        return this;
    }

    public FunctionAction getThenAction(String name) {
        return functions.get(name);
    }

    public SupplierAction getCallAction(String name) {
        return suppliers.get(name);
    }

    public void saveResults(String name, CompletableFuture<Object> future) {
        results.put(name, future);
    }

    public CompletableFuture getFuture(String name) {
        return results.get(name);
    }

    public CompletableFuture getResult(String name) {
        return this.results.get(name);
    }

    public Action getLastAction() {
        return this.lastAction;
    }

    public void setLastAction(Action action) {
        this.lastAction = action;
    }

    public CompletableFuture close() {
        if (lastAction == null) {
            throw new RuntimeException("lastAction is null");
        }
        CompletableFuture future = this.results.get(lastAction.getName());
        this.lastAction = null;
        return future;
    }
}
