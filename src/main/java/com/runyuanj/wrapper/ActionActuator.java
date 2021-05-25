package com.runyuanj.wrapper;

import com.runyuanj.action.Action;
import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.register.ActionDefinitionContainer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.runyuanj.util.ActionUtil.GET_ACTION_FUTURE;

/**
 * Action执行器
 * 封装CompletableFuture 与 ActionDefinitionContainer
 *
 */
public class ActionActuator {

    private ActionDefinitionContainer container;

    private ActionActuator() {}

    public static ActionActuator build(ActionDefinitionContainer container) {
        ActionActuator actuator = new ActionActuator();
        actuator.container = container;
        return actuator;
    }

    public ActionActuator call(String... names) {
        Arrays.stream(names).forEach((name) -> call(name));
        return this;
    }

    public ActionActuator andThen(String then, Object... props) {
        Action beforeAction = container.getLastAction();
        return then(then, beforeAction.getName(), props);
    }

    /**
     * warn: 默认将上一个请求的结果赋给第一个入参,
     * 请确认lambda表达式中的方法入参index是否正确, 或对方法进行一次封装.
     *
     * @param then
     * @param props
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public ActionActuator andThenOfSelf(String then, Object... props) throws ExecutionException, InterruptedException {
        Action beforeAction = container.getLastAction();
        CompletableFuture future = container.getFuture(beforeAction.getName());
        Object result = future.get();
        return then(then, beforeAction.getName(), result, props);
    }

    public ActionActuator then(String then, String before, Object... props) {
        CompletableFuture future = container.getFuture(before);
        FunctionAction action = container.getFunctionAction(then);
        action.setProps(props);

        CompletableFuture resultFuture = future.thenApplyAsync(action.getAction());
        container.saveResults(then, resultFuture);
        container.setLastAction(action);
        return this;
    }

    /**
     * warn: 默认将上一个请求的结果赋给第一个入参,
     * 请确认lambda表达式中的方法入参index是否正确, 或对方法进行一次封装.
     *
     * @param then
     * @param before
     * @param props
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public ActionActuator thenOfSelf(String then, String before, Object... props) throws ExecutionException, InterruptedException {
        CompletableFuture future = container.getFuture(before);
        Object result = future.get();
        return then(then, before, result, props);
    }

    public ActionActuator call(String name) {
        SupplierAction action = container.getSupplierAction(name);
        CompletableFuture future = CompletableFuture.supplyAsync(action.getAction());
        container.saveResults(name, future);
        container.setLastAction(action);
        return this;
    }

    public ActionActuator callOfParam(String name, Object... props) throws ExecutionException, InterruptedException {
        SupplierAction action = container.getSupplierAction(name);
        Object[] newProps = new Object[props.length];
        if (props != null && props.length > 0) {
            for (int i = 0; i < props.length; i++) {
                Object prop = props[i];
                if (prop != null && prop instanceof FutureNameWrapper) {
                    CompletableFuture future = container.getFuture(((FutureNameWrapper) prop).getName());
                    newProps[i] = future == null ? null : future.get();
                    continue;
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

    public CompletableFuture closeBranch() {
        if (container != null) {
            return container.closeBranch();
        }
        return null;
    }

    public void removeAction(String name) {
        this.container.removeAction(name);
    }

    public ActionActuator anyOf(String name, String ...before) {
        FunctionAction action = container.getFunctionAction(name);
        CompletableFuture[] f = new CompletableFuture[before.length];
        Arrays.stream(before).map((actionName) -> container.getResult(actionName)).collect(Collectors.toList()).toArray(f);

        CompletableFuture future = CompletableFuture.anyOf(f);
        future.thenApply(action.getAction());
        container.saveResults(name, future);
        container.setLastAction(action);
        return this;
    }

    /**
     * 用法:
     * anyOfParam().oneOf().async()
     * anyOfParam().oneOf().sync()
     *
     * Action:
     * FunctionAction action = new FunctionAction("actionA")
     *
     * Action function:
     * (preResult) -> {
     *     preResult.doSomething()
     *     // do(str1, str2, preResult)
     *     do(action.getProp(0, String.class), action.getProp(1, String.class), preResult)
     * }
     *
     * ActionActuator:
     * .call("preAction1", "preAction2")
     * .anyOfParam("actionA", "str1", "str2").oneOf("preAction1", "preAction2").sync()
     * .closeBranch()
     *
     * @param name
     * @param props
     * @return
     */
    public ActionActuator anyOfParam(String name, Object ...props) {
        Action action = container.getUncheckedAction(name);
        action.setProps(props);
        container.setLastAction(action);
        return this;
    }

    public ActionActuator with(String ...names) {
        CompletableFuture[] preFutures = new CompletableFuture[names.length];
        Arrays.stream(names).map((name) -> this.container.getResult(name)).collect(Collectors.toList()).toArray(preFutures);
        CompletableFuture<Object> future = CompletableFuture.anyOf(preFutures);
        this.container.saveResults(this.container.getLastAction().getName(), future);
        return this;
    }

    public ActionActuator sync() {
        Action anyOfAction = this.container.getLastAction();
        CompletableFuture future = this.container.getResult(anyOfAction.getName());
        // 假设FunctionAction只接收preFutures的返回值并不做额外处理, 并且在此处同步等待结果
        if (anyOfAction instanceof FunctionAction) {
             future.thenApply(((FunctionAction) anyOfAction).getAction());
        }
        container.saveResults(anyOfAction.getName(), future);
        return this;
    }
}
