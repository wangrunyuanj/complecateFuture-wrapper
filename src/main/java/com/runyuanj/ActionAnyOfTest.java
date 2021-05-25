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
        // 定义Action, 不需要带泛型
        SupplierAction actionA = new SupplierAction<>("actionA", () -> searchD("actionA"));
        SupplierAction actionB = new SupplierAction<>("actionB", () -> searchD("actionB"));

        // lambda的调用主体必须带泛型
        // <CInfo> in left is necessary
        FunctionAction<DInfo, CInfo> actionC = new FunctionAction("actionC");
        actionC.setAction((data) -> fetchC(data.getName()));

        ActionDefinitionContainer container = new ActionDefinitionContainer();
        container.addSupplier(actionA, actionB).addFunction(actionC);

        CompletableFuture future = ActionActuator.build(container).call("actionA", "actionB")
                // .anyOf("actionC", "actionA", "actionB")
                .anyOfParam("actionC").oneOf("actionA", "actionB").sync()
                .closeBranch();

        DInfo d = (DInfo) future.get();
        d.say();
    }


    public static DInfo searchD(String from) {
        try {
            Thread.sleep(1000);
            DInfo dInfo = new DInfo();
            dInfo.setName("D from " + from);
            return dInfo;
        } catch (InterruptedException e) {
            throw new RuntimeException("A Exception");
        }
    }

    public static CInfo fetchC(String name) {
        try {
            Thread.sleep(500);
            System.out.println(name);
            return new CInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("E Exception");
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
