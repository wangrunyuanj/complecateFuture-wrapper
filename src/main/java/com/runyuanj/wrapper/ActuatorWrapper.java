package com.runyuanj.wrapper;

import com.runyuanj.action.Action;
import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.register.ActionDefinitionContainer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.runyuanj.util.ActionUtil.GET_ACTION_FUTURE;

/**
 * 封装CompletableFuture
 */
public class ActuatorWrapper {

    private ActionDefinitionContainer container;

    private ActuatorWrapper() {}

    public static ActuatorWrapper build(ActionDefinitionContainer container) {
        ActuatorWrapper wrapper = new ActuatorWrapper();
        wrapper.container = container;
        return wrapper;
    }

    public ActuatorWrapper call(String... names) {
        Arrays.stream(names).forEach((name) -> call(name));
        return this;
    }

    public ActuatorWrapper andThen(String then, Object... props) {
        Action beforeAction = container.getLastAction();
        return then(then, beforeAction.getName(), props);
    }

    public ActuatorWrapper andThenOfSelf(String then, Object... props) throws ExecutionException, InterruptedException {
        Action beforeAction = container.getLastAction();
        CompletableFuture future = container.getFuture(beforeAction.getName());
        Object result = future.get();
        return then(then, beforeAction.getName(), result, props);
    }

    public ActuatorWrapper then(String then, String before, Object... props) {
        CompletableFuture future = container.getFuture(before);
        FunctionAction action = container.getThenAction(then);
        action.setProps(props);

        CompletableFuture resultFuture = future.thenApplyAsync(action.getAction());
        container.saveResults(then, resultFuture);
        container.setLastAction(action);
        return this;
    }

    public ActuatorWrapper thenOfSelf(String then, String before, Object... props) throws ExecutionException, InterruptedException {
        CompletableFuture future = container.getFuture(before);
        Object result = future.get();
        return then(then, before, result, props);
    }

    public ActuatorWrapper call(String name) {
        SupplierAction action = container.getCallAction(name);
        CompletableFuture future = CompletableFuture.supplyAsync(action.getAction());
        container.saveResults(name, future);
        container.setLastAction(action);
        return this;
    }

    public ActuatorWrapper callOfParam(String name, Object... props) throws ExecutionException, InterruptedException {
        SupplierAction action = container.getCallAction(name);
        Object[] newProps = new Object[props.length];
        if (props != null && props.length > 0) {
            for (int i = 0; i < props.length; i++) {
                Object prop = props[i];
                if (prop instanceof String) {
                    if (((String) prop).startsWith(GET_ACTION_FUTURE)) {
                        CompletableFuture future = container.getFuture(((String) prop).substring(6));
                        newProps[i] = future.get();
                        continue;
                    }
                }
                newProps[i] = props[i];
            }
        }
        action.setProps(newProps);
        CompletableFuture result = CompletableFuture.supplyAsync(action.getAction());
        container.saveResults(name, result);
        container.setLastAction(action);
        return this;
    }

    public CompletableFuture getResult(String name) {
        return container.getResult(name);
    }

    public CompletableFuture close() {
        return container.close();
    }
}
