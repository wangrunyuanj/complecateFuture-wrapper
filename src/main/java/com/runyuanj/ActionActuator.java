package com.runyuanj;

import com.runyuanj.action.Action;
import com.runyuanj.action.ConsumerAction;
import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.register.ActionDefinitionContainer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Action执行器
 * 封装CompletableFuture 与 ActionDefinitionContainer
 */
public class ActionActuator {

    private ActionDefinitionContainer container;

    private ActionActuator() {
    }

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
        Action action = container.getUncheckedAction(then);
        action.setProps(props);

        CompletableFuture resultFuture;
        if (action instanceof FunctionAction) {
            resultFuture = future.thenApplyAsync(((FunctionAction) action).getAction());
        } else if (action instanceof ConsumerAction) {
            resultFuture = future.thenAcceptAsync(((ConsumerAction) action).getAction());
        } else if (action instanceof SupplierAction) {
            resultFuture = CompletableFuture.supplyAsync(((SupplierAction) action).getAction());
        } else {
            throw new RuntimeException("Action is not a Function , Supplier or Consumer, please check your lambda");
        }

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

    /**
     * 清除最后一个游标, 并返回future
     * 但是不会清除action中的props.
     *
     * @return
     */
    public CompletableFuture closeBranch() {
        if (container != null) {
            return container.closeBranch();
        }
        return null;
    }

    public void removeAction(String name) {
        this.container.removeAction(name);
    }

    /**
     * 接收before的返回数据data并等待action执行完成, 将最终的data返回.
     * data可能在action执行过程中改变
     * 默认使用同步的方式处理, 保证future.get()的结果符合预期
     *
     * @param name
     * @param before
     * @return
     */
    public ActionActuator anyOf(String name, String... before) {
        return this.anyOfParam(name).oneOf(before).sync();
    }

    /**
     * 用法:
     * anyOfParam().oneOf().async()
     * anyOfParam().oneOf().sync()
     * <p>
     * Action:
     * FunctionAction action = new FunctionAction("actionA")
     * <p>
     * Action function:
     * (preResult) -> {
     * preResult.doSomething()
     * // do(str1, str2, preResult)
     * do(action.getProp(0, String.class), action.getProp(1, String.class), preResult)
     * }
     * <p>
     * ActionActuator:
     * .call("preAction1", "preAction2")
     * .anyOfParam("actionA", "str1", "str2").oneOf("preAction1", "preAction2").sync()
     * .closeBranch()
     *
     * @param name
     * @param props
     * @return
     */
    public ActionActuator anyOfParam(String name, Object... props) {
        Action action = container.getUncheckedAction(name);
        action.setProps(props);
        container.setLastAction(action);
        return this;
    }

    public ActionActuator oneOf(String... names) {
        CompletableFuture[] preFutures = new CompletableFuture[names.length];
        Arrays.stream(names).map((name) -> this.container.getResult(name)).collect(Collectors.toList()).toArray(preFutures);
        CompletableFuture<Object> future = CompletableFuture.anyOf(preFutures);
        this.container.saveResults(this.container.getLastAction().getName(), future);
        return this;
    }

    /**
     * 当action的入参data可能被改变时, 必须使用sync
     * 在action执行完成前阻塞, 保证future.get()的结果data符合预期
     * 否则data会可能发生不可预知的变化.
     * 当data不可变时, 可以使用async()
     *
     * @return
     */
    public ActionActuator sync() {
        Action action = this.container.getLastAction();
        CompletableFuture future = this.container.getResult(action.getName());
        // 假设FunctionAction只接收preFutures的返回值并不做额外处理, 并且在此处同步等待结果
        if (action instanceof FunctionAction) {
            future.thenApply(((FunctionAction) action).getAction());
        } else if (action instanceof ConsumerAction) {
            future.thenAccept(((ConsumerAction) action).getAction());
        } else {
            throw new RuntimeException("Action " + action.getName() + " is not FunctionAction or ConsumerAction");
        }
        container.saveResults(action.getName(), future);
        return this;
    }

    /**
     * 当入参data不改变时, 异步执行action并立刻返回data
     *
     * @return
     */
    public ActionActuator async() {
        Action action = this.container.getLastAction();
        CompletableFuture future = this.container.getResult(action.getName());
        // 假设FunctionAction只接收preFutures的返回值并不做额外处理, 并且在此处同步等待结果
        if (action instanceof FunctionAction) {
            future.thenApplyAsync(((FunctionAction) action).getAction());
        } else if (action instanceof ConsumerAction) {
            future.thenAcceptAsync(((ConsumerAction) action).getAction());
        } else {
            throw new RuntimeException("Action " + action.getName() + " is not FunctionAction or ConsumerAction");
        }
        container.saveResults(action.getName(), future);
        return this;
    }
}
