package com.runyuanj;

import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.model.*;
import com.runyuanj.register.ActionDefinitionContainer;
import com.runyuanj.wrapper.ActuatorWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.runyuanj.util.ActionUtil.getFuture;

public class ActionTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ActionDefinitionContainer container = new ActionDefinitionContainer();
        SupplierAction<AInfo> actionA = new SupplierAction<>("actionA", () -> fetchA());
        SupplierAction<BInfo> actionB = new SupplierAction<>("actionB", () -> fetchB());

        SupplierAction actionC = new SupplierAction("actionC");
        actionC.setAction(() -> fetchC((AInfo) actionC.getProps()[0], (BInfo) actionC.getProps()[1], (String) actionC.getProps()[2]));

        FunctionAction actionD = new FunctionAction("actionD", (info) -> {
            CInfo c = (CInfo) info;
            c.say();
            return fetchD();
        });
        FunctionAction actionE = new FunctionAction("actionE", (info) -> {
            DInfo d = (DInfo) info;
            return fetchE(d); // d.say()
        });

        container.addSupplier(actionA, actionB, actionC).addFunction(actionD, actionE);

        // 执行
        CompletableFuture close = ActuatorWrapper.build(container)
                .call("actionA", "actionB")
                .callOfParam("actionC", getFuture("actionA"), getFuture("actionB"), "name-c")
                .after("actionD")
                .after("actionE")
                .close();

        EInfo e = (EInfo) close.get();
        e.say();
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
