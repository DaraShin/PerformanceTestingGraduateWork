package com.shinkevich.benchmark_java;

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

/**
 * Number of tasks = number of threads in pool.
 * Time of pool creation is measured.
 */
@RunWith(value = Parameterized.class)
public class PiPoolCreationMeasuredBenchmark {
    private static final String TAG = PiPoolCreationMeasuredBenchmark.class.getSimpleName();

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    @Parameterized.Parameter
    public int stepsNum;

    @Parameterized.Parameters
    public static Collection<Object[]> stepsNumValues() {
        return Arrays.asList(new Object[][]{
                        {(int) Math.pow(10, 4)},
                        {(int) Math.pow(10, 5)},
                        {(int) Math.pow(10, 6)},
                        {(int) Math.pow(10, 7)},
                        {(int) Math.pow(10, 8)}
                }
        );
    }

    @Test
    public void executorService_1thread() throws InterruptedException, ExecutionException {
        BenchmarkState state = benchmarkRule.getState();
        int threadsNum = 1;
        while (state.keepRunning()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            double pi = 0;
            List<Future<Double>> resultFutures = new ArrayList<>(threadsNum);
            for (int i = 0; i < threadsNum; i++) {
                int threadNum = i;
                resultFutures.add(i, executor.submit(() ->
                        PiCalculator.calculatePiThreadTask(threadNum, stepsNum, threadsNum)
                ));
            }
            for (Future<Double> threadResult : resultFutures) {
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
    public void executorService_2threads() throws InterruptedException, ExecutionException {
        executorServiceTest(2);
    }

    @Test
    public void executorService_4threads() throws InterruptedException, ExecutionException {
        executorServiceTest(4);
    }

    @Test
    public void executorService_8threads() throws InterruptedException, ExecutionException {
        executorServiceTest(8);
    }

    private void executorServiceTest(int threadsNum) throws ExecutionException, InterruptedException {
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            ExecutorService executor = Executors.newFixedThreadPool(threadsNum);
            double pi = 0;
            List<Future<Double>> resultFutures = new ArrayList<>(threadsNum);
            for (int i = 0; i < threadsNum; i++) {
                int threadNum = i;
                resultFutures.add(i, executor.submit(() ->
                        PiCalculator.calculatePiThreadTask(threadNum, stepsNum, threadsNum)
                ));
            }
            for (Future<Double> threadResult : resultFutures) {
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
            executor.shutdown();
        }
    }

    @Test
    public void rxJava_1thread() {
        int threadsNum = 1;
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            List<SingleSource<Double>> resultObservables = new ArrayList<>(threadsNum);
            Scheduler scheduler = Schedulers.single();
            for (int i = 0; i < threadsNum; i++) {
                int threadNum = i;
                resultObservables.add(i, Single.fromCallable(() -> PiCalculator.calculatePiThreadTask(threadNum, stepsNum, threadsNum)));
            }
            double pi = Single
                    .merge((Iterable<SingleSource<Double>>) resultObservables)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .reduce(0.0, (x, y) -> x + y)
                    .blockingGet();
            benchmarkRule.getScope().runWithTimingDisabled((Function0<Void>) () -> {
                Assert.assertTrue(Math.abs(pi - Math.PI) < Tests.getPrecision(stepsNum));
                scheduler.shutdown();
                return null;
            });
        }
    }

    @Test
    public void rxJava_2threads() {
        rxJavaTest(2);
    }

    @Test
    public void rxJava_4threads() {
        rxJavaTest(4);
    }

    @Test
    public void rxJava_8threads() {
        rxJavaTest(8);
    }

    private void rxJavaTest(int threadsNum) {
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
            Scheduler scheduler = Schedulers.from(executorService);
            List<SingleSource<Double>> resultObservables = new ArrayList<>(threadsNum);
            for (int i = 0; i < threadsNum; i++) {
                int threadNum = i;
                resultObservables.add(i, Single.fromCallable(() -> PiCalculator.calculatePiThreadTask(threadNum, stepsNum, threadsNum)));
            }
            double pi = Single
                    .merge((Iterable<SingleSource<Double>>) resultObservables)
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .reduce(0.0, (x, y) -> x + y)
                    .blockingGet();
            benchmarkRule.getScope().runWithTimingDisabled((Function0<Void>) () -> {
                Assert.assertTrue(Math.abs(pi - Math.PI) < Tests.getPrecision(stepsNum));
                executorService.shutdown();
                scheduler.shutdown();
                return null;
            });
        }
    }
}
