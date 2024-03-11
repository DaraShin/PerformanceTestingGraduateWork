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
public class PiSequentialBenchmark {
    private static final String TAG = PiSequentialBenchmark.class.getSimpleName();
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
    public void sequentialMainThread() {
        BenchmarkState state = benchmarkRule.getState();
        double pi = 0;
        while (state.keepRunning()) {
            pi = PiCalculator.calculatePiThreadTask(0, stepsNum, 1);
        }
        Log.d(TAG, "calculated pi = " + pi);
        Assert.assertTrue(Math.abs(pi - Math.PI) < Tests.getPrecision(stepsNum));
    }
}
