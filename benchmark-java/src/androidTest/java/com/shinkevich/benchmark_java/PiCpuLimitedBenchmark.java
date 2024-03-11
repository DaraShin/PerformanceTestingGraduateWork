package com.shinkevich.benchmark_java;

import android.util.Log;

import androidx.benchmark.junit4.BenchmarkRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(value = Parameterized.class)
public class PiCpuLimitedBenchmark {
    private static final String TAG  = PiCpuLimitedBenchmark.class.getSimpleName();

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
                        {(int)Math.pow(10,8)},
                }
        );
    }

    @Test
    public void executorService_cpuLimitedThreads() throws InterruptedException, ExecutionException {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "processors number: " + processorsNum);
        Tests.executorServiceTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), processorsNum, Executors.newFixedThreadPool(processorsNum));
    }

    @Test
    public void rxJava_cpuLimitedThreads()  {
        int processorsNum = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "processors number: " + processorsNum);
        Tests.rxJavaTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), processorsNum, Schedulers.computation());
    }
}
