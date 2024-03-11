package com.shinkevich.benchmark_java;

import androidx.benchmark.junit4.BenchmarkRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Number of tasks = number of threads in pool.
 * Time of pool creation is not measured.
 */
@RunWith(value = Parameterized.class)
public class PiPoolCreationNotMeasuredBenchmark {
    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    @Parameterized.Parameter
    public int stepsNum;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                        {(int)Math.pow(10,4)},
                        {(int)Math.pow(10,5)},
                        {(int)Math.pow(10,6)},
                        {(int)Math.pow(10,7)},
                        {(int)Math.pow(10,8)}
                }
        );
    }

    @Test
    public void executorService_1thread() throws InterruptedException, ExecutionException {
        Tests.executorServiceTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), 1, Executors.newSingleThreadExecutor());
    }

    @Test
    public void executorService_2threads() throws InterruptedException, ExecutionException {
        Tests.executorServiceTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),2, Executors.newFixedThreadPool(2));
    }

    @Test
    public void executorService_4threads() throws InterruptedException, ExecutionException {
        Tests.executorServiceTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),4, Executors.newFixedThreadPool(4));
    }

    @Test
    public void executorService_8threads() throws InterruptedException, ExecutionException {
        Tests.executorServiceTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), 8, Executors.newFixedThreadPool(8));
    }



//    @Test
//    public void rxJava_mainThread()  {
//        rxJavaTest(1, AndroidSchedulers.mainThread());
//    }

    @Test
    public void rxJava_1thread()  {
        Tests.rxJavaTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), 1, Schedulers.single());
    }

    @Test
    public void rxJava_2threads()  {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Scheduler scheduler = Schedulers.from(executorService);
        Tests.rxJavaTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), 2, scheduler);
    }

    @Test
    public void rxJava_4threads()  {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Scheduler scheduler = Schedulers.from(executorService);
        Tests.rxJavaTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), 4, scheduler);
    }

    @Test
    public void rxJava_8threads()  {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        Scheduler scheduler = Schedulers.from(executorService);
        Tests.rxJavaTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), 8, scheduler);
    }
}
