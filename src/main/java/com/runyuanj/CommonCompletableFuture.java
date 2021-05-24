package com.runyuanj;

import com.runyuanj.model.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CommonCompletableFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<AInfo> ra = CompletableFuture.supplyAsync(() -> fetchA());
        CompletableFuture<BInfo> rb = CompletableFuture.supplyAsync(() -> fetchB());

        AInfo aInfo = ra.get();
        BInfo bInfo = rb.get();
        CompletableFuture<CInfo> rc = CompletableFuture.supplyAsync(() -> fetchC(aInfo, bInfo));
        CompletableFuture<DInfo> rd = rc.thenApplyAsync(cInfo -> {
            cInfo.say();
            return fetchD();
        });

        // dispatch(m)
        CompletableFuture<MInfo> rm = CompletableFuture.supplyAsync(() -> fetchM());
        // rm.next(n)
        CompletableFuture<NInfo> rn = rm.thenApplyAsync((mInfo -> {
            mInfo.say();
            return fetchN();
        }));

        CompletableFuture<EInfo> re = CompletableFuture.supplyAsync(() -> fetchE());

        EInfo eInfo = re.get();
        NInfo nInfo = rn.get();
        DInfo dInfo = rd.get();
        CompletableFuture<FInfo> rf = CompletableFuture.supplyAsync(() ->  {
            eInfo.say();
            nInfo.say();
            dInfo.say();
            return fetchF();
        });
        rf.get().say();
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

    public static CInfo fetchC(AInfo a, BInfo b) {
        try {
            Thread.sleep(100);
            a.say();
            b.say();
            return new CInfo();
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

    public static EInfo fetchE() {
        try {
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

    public static MInfo fetchM() {
        try {
            Thread.sleep(100);
            return new MInfo();
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
