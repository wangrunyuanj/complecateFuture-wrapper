package com.runyuanj;

import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.model.*;
import com.runyuanj.register.ActionDefinitionContainer;
import com.runyuanj.wrapper.ActionActuator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.runyuanj.util.ActionUtil.getAnyOf;

/**
 * 测试 A, B 完成任何一个, 启动 C
 */
public class ActionAnyOfTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
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

        ActionDefinitionContainer container = new ActionDefinitionContainer();
        container.addSupplier(actionA, actionB).addFunction(actionC);

        CompletableFuture future = ActionActuator.build(container).call("actionA", "actionB")
                // .anyOf("actionD", "actionA", "actionB")
                .anyOfParam("actionC").oneOf("actionA", "actionB").async()
                .closeBranch();

        CInfo c = (CInfo) future.get();
        c.say();
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

    public static CInfo fetchC(String name) {
        try {
            Thread.sleep(500);
            System.out.println("In fetchC: " + name);
            return new CInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("E Exception");
        }
    }

    public static DInfo fetchD(String name) {
        try {
            Thread.sleep(500);
            System.out.println("search Result: " + name);
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
