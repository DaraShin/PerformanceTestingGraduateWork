package com.shinkevich.benchmark_java;

import androidx.benchmark.BenchmarkState;
import androidx.benchmark.junit4.BenchmarkRule;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import kotlin.jvm.functions.Function0;

public class Tests {
    static void executorServiceTest(BenchmarkRule benchmarkRule, int stepsNum, double precision, int threadsNum, ExecutorService executor) throws ExecutionException, InterruptedException {
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            double pi = 0;
            List<Future<Double>> resultFutures = new ArrayList<>(threadsNum);
            CompletableFuture c = CompletableFuture.supplyAsync(new Supplier<Object>() {
                @Override
                public Object get() {
                    return null;
                }
            }, executor);
            c.get();
            for (int i = 0; i < threadsNum; i++) {
                int threadNum = i;
                resultFutures.add(i, executor.submit(() ->
                        PiCalculator.calculatePiThreadTask(threadNum, stepsNum, threadsNum)
                ));
            }
            for(Future<Double> threadResult : resultFutures){
                pi += threadResult.get();
            }
            double finalPi = pi;
            benchmarkRule.getScope().runWithTimingDisabled((Function0<Void>) () -> {
                Assert.assertTrue(Math.abs(finalPi - Math.PI) < precision);
                return null;
            });

        }
    }

    static void rxJavaTest(BenchmarkRule benchmarkRule, int stepsNum, double precision, int threadsNum, Scheduler scheduler) {
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            List<SingleSource<Double>> resultObservables = new ArrayList<>(threadsNum);
            for(int i = 0; i < threadsNum; i++){
                int threadNum = i;
                resultObservables.add(i, Single.fromCallable(() -> PiCalculator.calculatePiThreadTask(threadNum, stepsNum, threadsNum)).subscribeOn(scheduler));
            }
            double pi = Single
                    .merge((Iterable<SingleSource<Double>>)resultObservables)
                    //.subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .reduce(0.0, (x,y)->x+y)
                    .blockingGet();
            benchmarkRule.getScope().runWithTimingDisabled((Function0<Void>) () -> {
                Assert.assertTrue(Math.abs(pi - Math.PI) < Tests.getPrecision(stepsNum));
                return null;
            });
        }
    }

    static double getPrecision(int stepsNum){
        return 100.0 / stepsNum;
    }
}
