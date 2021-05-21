package com.runyuanj.wrapper;

import com.runyuanj.register.ActionRegister;
import com.runyuanj.register.PayloadsWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class CompletableFutureWrapper {

    public ActionRegister register;
    public PayloadsWrapper payloads;

    public CompletableFutureWrapper (ActionRegister register, PayloadsWrapper payloads){
        this.register = register;
        this.payloads = payloads;
    }

    public <U, T> CompletableFuture<U> then(String action, CompletableFuture<T> future, Object... objects) {
        payloads.setActionProps(action, objects);
        return future.thenApplyAsync(register.getFunction(action));
    }

    public <U, T> CompletableFuture<U> thenPass(String action, CompletableFuture<T> future, Object... objects) throws ExecutionException, InterruptedException {
        T t;
        try {
            t = future.get();
        } catch (ExecutionException e) {
            throw new ExecutionException(action + " throws ExecutionException", e.getCause());
        } catch (InterruptedException e) {
            throw new InterruptedException(action + " throws InterruptedException");
        }
        payloads.setActionProps(action, t, objects);
        return future.thenApplyAsync(register.getFunction(action));
    }

    public <T> CompletableFuture<T> call(String action) {
        Supplier<T> supplier = register.getSupplier(action);
        return CompletableFuture.supplyAsync(supplier);
    }

    public <T> CompletableFuture<T> call(String action, Object... objects) {
        payloads.setActionProps(action, objects);

        Supplier<T> supplier = register.getSupplier(action);
        return CompletableFuture.supplyAsync(supplier);
    }

    public Object param(String action) throws ExecutionException, InterruptedException {
        return call(action).get();
    }

    public void destroy() {
        this.payloads.destroy();
        this.register = null;
    }
}
