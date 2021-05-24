package com.runyuanj.register;


import com.runyuanj.action.Action;
import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 需要包含namespace
 */
public class ActionDefinitionContainer {

    private Map<String, SupplierAction> suppliers = new HashMap<>();

    private Map<String, FunctionAction> functions = new HashMap<>();

    private Map<String, CompletableFuture> results = new HashMap<>();

    private Action beforeAction;

    private CompletableFuture endCursor = null;

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

    public void setResultCursor(CompletableFuture resultFuture) {
        this.endCursor = resultFuture;
    }

    public CompletableFuture close() {
        if (endCursor == null) {
            throw new RuntimeException("请使用getResult(String actionName)");
        }
        CompletableFuture future = this.endCursor;
        this.endCursor = null;
        this.beforeAction = null;
        return future;
    }

    public CompletableFuture getResult(String name) {
        return this.results.get(name);
    }

    public Action getBeforeAction() {
        return this.beforeAction;
    }

    public void setBeforeAction(Action action) {
        this.beforeAction = action;
    }
}
