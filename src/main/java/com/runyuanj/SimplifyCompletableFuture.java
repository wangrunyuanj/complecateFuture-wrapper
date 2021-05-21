package com.runyuanj;

import com.runyuanj.model.*;
import com.runyuanj.register.ActionRegister;
import com.runyuanj.wrapper.CompletableFutureWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SimplifyCompletableFuture {

    private static ActionRegister register = new ActionRegister();

    public static void createRegister() {
        register.register("actionA", () -> fetchA());
        register.register("actionB", () -> fetchB());
        register.register("actionF", () -> fetchF());
        register.registerFunctions("actionD", info -> fetchD());
        register.register("actionM", () -> fetchM(register.take("actionM", 0, String.class)));
        register.registerFunctions("actionN", info -> fetchN());
        register.registerFunctions("actionE", info -> fetchE(register.take("actionE", 0, DInfo.class)));
        register.register("actionC", () -> {
            System.out.println(register.take("actionC", 2, String.class));
            return fetchC(
                    register.take("actionC", 0, AInfo.class),
                    register.take("actionC", 1, BInfo.class),
                    register.take("actionC", 2, String.class));
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFutureWrapper wrapper = new CompletableFutureWrapper(register);

        long start = System.currentTimeMillis();

        // c wait (a, b), then d, then e (d as the first param)
        // CompletableFuture<DInfo> d = then("actionD", call("actionC", param("actionA"), param("actionB"), "name-c"));
        // CompletableFuture<EInfo> e = thenPass("actionE", d);
        CompletableFuture<EInfo> e = wrapper.thenPass("actionE", wrapper.then("actionD", wrapper.call("actionC", wrapper.param("actionA"), wrapper.param("actionB"), "name-c")));

        // execute before m
        CompletableFuture<NInfo> n = wrapper.then("actionN", wrapper.call("actionM", "name-m"));

        // wait for re and n
        CompletableFuture<FInfo> f = wrapper.call("actionF", e.get(), n.get());

        e.get().say();
        f.get().say();
        n.get().say();

        long end = System.currentTimeMillis() - start;
        System.out.println(end);
        wrapper.destroy();
    }


    public static AInfo fetchA() {
        try {
            Thread.sleep(1000);
            return new AInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("A Exception");
        }
    }

    public static BInfo fetchB() {
        try {
            Thread.sleep(1000);
            return new BInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("B Exception");
        }
    }

    public static CInfo fetchC(AInfo a, BInfo b, String name) {
        try {
            Thread.sleep(100);
            a.say();
            b.say();
            return new CInfo(name);
        } catch (InterruptedException e) {
            throw new RuntimeException("C Exception");
        }
    }

    public static DInfo fetchD() {
        try {
            Thread.sleep(100);
            return new DInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("D Exception");
        }
    }

    public static EInfo fetchE(DInfo dInfo) {
        try {
            dInfo.say();
            Thread.sleep(100);
            return new EInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("E Exception");
        }
    }

    public static FInfo fetchF() {
        try {
            Thread.sleep(500);
            return new FInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("F Exception");
        }
    }

    public static MInfo fetchM(String name) {
        try {
            Thread.sleep(100);
            return new MInfo(name);
        } catch (InterruptedException e) {
            throw new RuntimeException("M Exception");
        }
    }

    public static NInfo fetchN() {
        try {
            Thread.sleep(100);
            return new NInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("N Exception");
        }
    }

}
