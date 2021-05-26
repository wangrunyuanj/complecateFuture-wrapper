package com.runyuanj.wrapper;

import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.model.*;
import com.runyuanj.register.ActionDefinitionContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.runyuanj.util.ActionUtil.getFuture;

public class ActionActuatorTest {

    @Test
    public void testThen() throws ExecutionException, InterruptedException {
        // 定义Action, 不需要带泛型
        SupplierAction actionA = new SupplierAction<>("actionA", () -> fetchA());
        SupplierAction actionB = new SupplierAction<>("actionB", () -> fetchB());

        // lambda的调用主体必须带泛型
        // <CInfo> in left is necessary
        SupplierAction<CInfo> actionC = new SupplierAction("actionC");
        actionC.setAction(() -> fetchC(
                // fetchC(): a.say(), b.say()
                actionC.getProp(0, AInfo.class),
                actionC.getProp(1, BInfo.class),
                actionC.getProp(2, String.class)
        ));

        // <CInfo, DInfo> in right is necessary
        FunctionAction actionD = new FunctionAction<CInfo, DInfo>("actionD", (c) -> {
            c.say();
            return fetchD();
        });

        // <DInfo, EInfo> in left is necessary
        FunctionAction<DInfo, EInfo> actionE = new FunctionAction("actionE");
        actionE.setAction((d) -> {
            d.say();
            return fetchE(actionE.getProp(0, String.class));
        });

        // 添加到容器
        ActionDefinitionContainer container = new ActionDefinitionContainer();
        container.addSupplier(actionA, actionB, actionC).addFunction(actionD, actionE);

        // 执行
        CompletableFuture result = ActionActuator.build(container)
                .call("actionA", "actionB")
                .callOfParam("actionC", getFuture("actionA"), getFuture("actionB"), "name-c")
                .andThen("actionD")
                .andThen("actionE", "name-e")
                .closeBranch();

        EInfo e = (EInfo) result.get();
        e.say();
    }

    @Test
    public void testThen2() throws ExecutionException, InterruptedException  {

    }

    @Test
    public void testAnyOf() throws ExecutionException, InterruptedException {
//        CompletableFuture a = CompletableFuture.supplyAsync(() -> searchC("actionA"));
//        CompletableFuture b = CompletableFuture.supplyAsync(() -> searchC("actionB"));
//
//        // anyOf 只返回一个data
//        CompletableFuture c = CompletableFuture.anyOf(a, b);
//        c.thenApplyAsync((data) -> {
//            CInfo cInfo = (CInfo) data;
//            cInfo.say();
//            cInfo.setName("?????"); // ??????????????????????????
//            return new DInfo(); // ??????????????????????????
//        });
//
//        CInfo rc = (CInfo) c.get();
//        rc.say();
//        Thread.sleep(3000);

        // 定义Action, 不需要带泛型
        SupplierAction actionA = new SupplierAction<>("actionA", () -> searchC("actionA"));
        SupplierAction actionB = new SupplierAction<>("actionB", () -> searchC("actionB"));

        // lambda的调用主体必须带泛型
        // <CInfo> in left is necessary
        FunctionAction actionC = new FunctionAction("actionC");
        actionC.setAction((data) -> {
            CInfo cInfo = (CInfo) data;
            cInfo.say();
            cInfo.setName("?????"); // ??????????????????????????
            return new DInfo(); // ??????????????????????????
        });

        ActionDefinitionContainer container = new ActionDefinitionContainer().addSupplier(actionA, actionB).addFunction(actionC);

        CompletableFuture future = ActionActuator.build(container).call("actionA", "actionB")
                // .anyOf("actionD", "actionA", "actionB")
                .anyOfParam("actionC").oneOf("actionA", "actionB").async()
                .closeBranch();

        CInfo c = (CInfo) future.get();
        c.say();
        Thread.sleep(2000);

        //actionA executed
        //This is C from actionA
        //This is C from actionA
        //actionB executed

        //actionB executed
        //actionA executed
        //This is C from actionB
        //This is ?????
    }

    public static CInfo searchC(String from) {
        try {
            Thread.sleep(1000);
            CInfo c = new CInfo();
            c.setName("C from " + from);
            System.out.println(from + " executed");
            return c;
        } catch (InterruptedException e) {
            throw new RuntimeException("A Exception");
        }
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

    public static EInfo fetchE(String name) {
        try {
            Thread.sleep(500);
            EInfo eInfo = new EInfo();
            eInfo.setName(name);
            return eInfo;
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
