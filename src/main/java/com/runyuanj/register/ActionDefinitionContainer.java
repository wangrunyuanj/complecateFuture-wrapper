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
        if (actions == null) {
            throw new RuntimeException("Action is null!");
        }

        Arrays.stream(actions).forEach((action) -> {
            validateAction(action);
            this.suppliers.put(action.getName(), action);
        });
        return this;
    }

    public ActionDefinitionContainer addFunction(FunctionAction... actions) {
        if (actions == null) {
            throw new RuntimeException("Action is null!");
        }

        Arrays.stream(actions).forEach((action) -> {
            validateAction(action);
            this.functions.put(action.getName(), action);
        });
        return this;
    }

    private void validateAction(Action action) {
        if (action == null) {
            throw new RuntimeException("Action " + action.getName() + " can't be null");
        }
        if (this.suppliers.containsKey(action.getName())) {
            throw new RuntimeException("SupplierAction " + action.getName() + " already exists");
        }
        if (this.functions.containsKey(action.getName())) {
            throw new RuntimeException("FunctionAction " + action.getName() + " already exists");
        }
    }

    public FunctionAction getFunctionAction(String name) {
        return this.validateAndGetFunctionAction(name);
    }

    public SupplierAction getSupplierAction(String name) {
        return this.validateAndGetSupplierAction(name);
    }

    private FunctionAction validateAndGetFunctionAction(String name) {
        if (!functions.containsKey(name)) {
            if (suppliers.containsKey(name)) {
                throw new RuntimeException("Action " + name + " is a SupplierAction!");
            }
            throw new RuntimeException("Action " + name + " not in container");
        } else {
            FunctionAction action = functions.get(name);
            if (action == null) {
                throw new RuntimeException("Action " + name + " is null");
            } else {
                return action;
            }
        }
    }

    private SupplierAction validateAndGetSupplierAction(String name) {
        if (!suppliers.containsKey(name)) {
            if (functions.containsKey(name)) {
                throw new RuntimeException("Action " + name + " is a FunctionAction!");
            }
            throw new RuntimeException("Action " + name + " not in container");
        } else {
            SupplierAction action = suppliers.get(name);
            if (action == null) {
                throw new RuntimeException("Action " + name + " is null");
            } else {
                return action;
            }
        }
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

    /**
     *
     * @return
     */
    public CompletableFuture closeBranch() {
        if (lastAction == null) {
            throw new RuntimeException("lastAction is null");
        }
        CompletableFuture future = this.results.get(lastAction.getName());
        this.lastAction = null;
        return future;
    }

    public void removeAction(String name) {
        if (lastAction.getName().equals(name)) {
            throw new RuntimeException("Action " + name + " can not remove");
        }
        this.results.remove(name);
        this.suppliers.remove(name);
        this.functions.remove(name);
    }

    public Action getUncheckedAction(String name) {
        if (suppliers.containsKey(name)) {
            return getSupplierAction(name);
        } else {
            return getFunctionAction(name);
        }
    }
}
