package com.runyuanj.wrapper;

import com.runyuanj.action.Action;
import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.model.CInfo;
import com.runyuanj.register.ActionDefinitionContainer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.runyuanj.util.ActionUtil.GET_ACTION_FUTURE;

public class ActuatorWrapper {

    private ActionDefinitionContainer container;

    public static ActuatorWrapper build(ActionDefinitionContainer container) {
        ActuatorWrapper wrapper = new ActuatorWrapper();
        wrapper.setContainer(container);
        return wrapper;
    }

    public void setContainer(ActionDefinitionContainer container) {
        this.container = container;
    }

    public ActuatorWrapper call(String... names) {
        Arrays.stream(names).forEach((name) -> call(name));
        return this;
    }

    public ActuatorWrapper after(String then, Object... props) {
        Action beforeAction = container.getBeforeAction();
        return then(then, beforeAction.getName(), props);
    }

    public ActuatorWrapper afterOfSelf(String then, Object... props) throws ExecutionException, InterruptedException {
        Action beforeAction = container.getBeforeAction();
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
        container.setResultCursor(resultFuture);
        container.setBeforeAction(action);
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
        container.setBeforeAction(action);
        return this;
    }

    public CompletableFuture getResult(String name) {
        return container.getResult(name);
    }

    public CompletableFuture close() {
        return container.close();
    }
}
