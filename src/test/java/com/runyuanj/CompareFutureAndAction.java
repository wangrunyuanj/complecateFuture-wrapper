package com.runyuanj;

import com.runyuanj.action.FunctionAction;
import com.runyuanj.action.SupplierAction;
import com.runyuanj.model.*;
import com.runyuanj.register.ActionDefinitionContainer;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.runyuanj.util.ActionUtil.getFuture;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@Fork(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class CompareFutureAndAction {

    /**
     * futureTest 与 actionTest 占用内存和时间几乎相等
     *
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CompareFutureAndAction.class.getSimpleName())
                .addProfiler(HotspotMemoryProfiler.class)
//                .addProfiler(HotspotThreadProfiler.class)
//                .addProfiler(StackProfiler.class)
                .build();
        new Runner(opt).run();
    }

    // @Test
    @Benchmark
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
        eInfo.say();
    }

    // @Test
    @Benchmark
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

    // @Test
    public void anyOfFutureTest() throws ExecutionException, InterruptedException, TimeoutException {
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

    // @Test
    public void allOfFutureTest() throws ExecutionException, InterruptedException {
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

    /**
     * consumer在修改入参data后, 如果是sync(), 那么future.get()得到的结果是修改后的data
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    // @Test
    public void anyOfFutureTest2() throws ExecutionException, InterruptedException {
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

        CompletableFuture c = CompletableFuture.anyOf(a, b);
        c.thenAccept((data) -> {
            AInfo aInfo = (AInfo) data;
            aInfo.setName("new c");
        });

        AInfo aInfo = (AInfo) c.get();
        aInfo.say();
    }

    public static AInfo fetchA() {
        try {
            Thread.sleep(200);
            return new AInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("A Exception");
        }
    }

    public static BInfo fetchB() {
        try {
            Thread.sleep(200);
            return new BInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("B Exception");
        }
    }

    public static CInfo fetchC(AInfo a, BInfo b, String name) {
        try {
            a.say();
            b.say();
            Thread.sleep(200);
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
            Thread.sleep(200);
            return new EInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException("E Exception");
        }
    }

    public static FInfo fetchF() {
        try {
            Thread.sleep(200);
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
