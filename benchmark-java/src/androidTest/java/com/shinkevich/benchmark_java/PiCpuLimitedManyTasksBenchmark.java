package com.shinkevich.benchmark_java;

import android.util.Log;

import androidx.benchmark.BenchmarkState;
import androidx.benchmark.junit4.BenchmarkRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.jvm.functions.Function0;

@RunWith(value = Parameterized.class)
public class PiCpuLimitedManyTasksBenchmark {
    private static final String TAG  = PiCpuLimitedManyTasksBenchmark.class.getSimpleName();

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    @Parameterized.Parameter
    public int stepsNum;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                        {(int)Math.pow(10,3)},
                        {(int)Math.pow(10,4)},
                        {(int)Math.pow(10,5)},
                        {(int)Math.pow(10,6)},
                }
        );
    }

    @Test
    public void sequential_mainThread_manyTasks() throws InterruptedException, ExecutionException {
        BenchmarkState state = benchmarkRule.getState();
        double pi = 0;
        while (state.keepRunning()) {
            pi = 0;
            for (int i = 0; i < stepsNum; i++) {
                pi += PiCalculator.calculateIntervalValue(i, stepsNum);
            }
        }
        Log.d(TAG, "pi = " + pi);
        Assert.assertTrue(Math.abs(pi - Math.PI) < Tests.getPrecision(stepsNum));
    }


    @Test
    public void executorService_cpuLimitedThreads_manyTasks() throws InterruptedException, ExecutionException {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "processors number: " + processorsNum);
        BenchmarkState state = benchmarkRule.getState();
        ExecutorService executor = Executors.newFixedThreadPool(processorsNum);
        executorService_cpuLimitedThreads_manyTasks_test(executor);
    }

    @Test
    public void executorServiceWorkStealingPool_cpuLimitedThreads_manyTasks() throws InterruptedException, ExecutionException {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "processors number: " + processorsNum);
        BenchmarkState state = benchmarkRule.getState();
        ExecutorService executor = Executors.newWorkStealingPool(processorsNum);
        executorService_cpuLimitedThreads_manyTasks_test(executor);
    }

    public void executorService_cpuLimitedThreads_manyTasks_test(ExecutorService executor) throws InterruptedException, ExecutionException {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "processors number: " + processorsNum);
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            double pi = 0;
            List<Future<Double>> resultFutures = new ArrayList<>(stepsNum);
            for (int i = 0; i < stepsNum; i++) {
                int stepNum = i;
                resultFutures.add(i, executor.submit(() ->
                        PiCalculator.calculateIntervalValue(stepNum, stepsNum)
                ));
            }
            for(Future<Double> threadResult : resultFutures){
                pi += threadResult.get();
            }
            double finalPi = pi;
            benchmarkRule.getScope().runWithTimingDisabled(new Function0<Void>() {
                @Override
                public Void invoke() {
                    Assert.assertTrue(Math.abs(finalPi - Math.PI) < Tests.getPrecision(stepsNum));
                    return null;
                }
            });
        }
    }

    @Test
    public void rxJava_cpuLimitedThreads_manyTasks() {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "processors number: " + processorsNum);
        BenchmarkState state = benchmarkRule.getState();
        Scheduler scheduler = Schedulers.computation();
        while (state.keepRunning()) {
            List<SingleSource<Double>> resultObservables = new ArrayList<>(stepsNum);
            for(int i = 0; i < stepsNum; i++){
                int stepNum = i;
                resultObservables.add(i, Single.fromCallable(() ->  PiCalculator.calculateIntervalValue(stepNum, stepsNum)));
            }
            double pi = Single
                    .merge((Iterable<SingleSource<Double>>)resultObservables)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .reduce(0.0, (x,y)->x+y)
                    .blockingGet();
            benchmarkRule.getScope().runWithTimingDisabled((Function0<Void>) () -> {
                Assert.assertTrue(Math.abs(pi - Math.PI) < Tests.getPrecision(stepsNum));
                return null;
            });
        }
    }
}
