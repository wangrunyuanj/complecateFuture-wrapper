package com.runyuanj.wrapper;

import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.model.*;
import com.runyuanj.register.ActionDefinitionContainer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.runyuanj.util.ActionUtil.getFuture;

public class CompareFutureAndAction {

    @Test
    public void actionTest() throws ExecutionException, InterruptedException {
        // 定义Action
        SupplierAction<AInfo> actionA = new SupplierAction<>("actionA", () -> fetchA());
        SupplierAction<BInfo> actionB = new SupplierAction<>("actionB", () -> fetchB());

        SupplierAction actionC = new SupplierAction("actionC");
        // fetchC(): a.say(), b.say()
        actionC.setAction(() -> fetchC((AInfo) actionC.getProps()[0], (BInfo) actionC.getProps()[1], (String) actionC.getProps()[2]));

        FunctionAction<CInfo, DInfo> actionD = new FunctionAction("actionD", (info) -> {
            CInfo c = (CInfo) info;
            c.say();
            return fetchD();
        });
        FunctionAction actionE = new FunctionAction("actionE", (info) -> {
            DInfo d = (DInfo) info;
            return fetchE(d); // d.say()
        });

        // 添加到容器
        ActionDefinitionContainer container = new ActionDefinitionContainer();
        container.addSupplier(actionA, actionB, actionC).addFunction(actionD, actionE);

        // 执行
        CompletableFuture result = ActionActuator.build(container)
                .call("actionA", "actionB")
                .callOfParam("actionC", getFuture("actionA"), getFuture("actionB"), "name-c")
                .andThen("actionD")
                .andThen("actionE")
                .closeBranch();

        EInfo e = (EInfo) result.get();
        e.say();
    }

    @Test
    public void futureTest2() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<AInfo> a = CompletableFuture.supplyAsync(() -> {
            AInfo aInfo = fetchA();
            aInfo.setName("from A");
            System.out.println("A is down");
            return aInfo;
        });
        CompletableFuture<AInfo> b = CompletableFuture.supplyAsync(() -> {
            AInfo aInfo = fetchA();
            aInfo.setName("from B");
            System.out.println("B is down");
            return aInfo;
        });

        CompletableFuture c = CompletableFuture.anyOf(a, b);
        c.thenApplyAsync((data) -> {
            AInfo aInfo = (AInfo) data;
            aInfo.say();
            return new AInfo(); // NO USE!!!
        });

        AInfo ra = (AInfo) c.get();
        ra.say();

        CompletableFuture<DInfo> d = CompletableFuture.supplyAsync(() -> {
            ra.setName("new A");
            DInfo dInfo = new DInfo();
            dInfo.setName("D from " + ra.getName());
            return dInfo;
        });

        DInfo dInfo = d.get();
        dInfo.say();
    }

    @Test
    public void futureTest3() throws ExecutionException, InterruptedException {
        CompletableFuture<AInfo> a = CompletableFuture.supplyAsync(() -> {
            AInfo aInfo = fetchA();
            System.out.println("A is down");
            return aInfo;
        });
        CompletableFuture<BInfo> b = CompletableFuture.supplyAsync(() -> {
            BInfo bInfo = fetchB();
            System.out.println("B is down");
            return bInfo;
        });

        CompletableFuture c = CompletableFuture.allOf(a, b);
        c.handle((a1, b1) -> {
            System.out.println(a1); // null
            System.out.println(b1); // null
            return new CInfo();
        });
        c.join();
    }

    @Test
    public void futureTest() throws ExecutionException, InterruptedException {
        CompletableFuture<AInfo> a = CompletableFuture.supplyAsync(() -> fetchA());
        CompletableFuture<BInfo> b = CompletableFuture.supplyAsync(() -> fetchB());

        AInfo aInfo = a.get();
        BInfo bInfo = b.get();
        CompletableFuture<CInfo> c = CompletableFuture.supplyAsync(() -> fetchC(aInfo, bInfo, "name-c"));
        CompletableFuture<DInfo> d = c.thenApplyAsync(cInfo -> {
            cInfo.say();
            return fetchD();
        });

        DInfo dInfo = d.get();
        CompletableFuture<EInfo> e = CompletableFuture.supplyAsync(() -> fetchE(dInfo));

        EInfo eInfo = e.get();
        CompletableFuture<FInfo> f = CompletableFuture.supplyAsync(() ->  {
            eInfo.say();
            return fetchF();
        });
        f.get().say();
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
            a.say();
            b.say();
            Thread.sleep(2000);
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
            Thread.sleep(500);
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
