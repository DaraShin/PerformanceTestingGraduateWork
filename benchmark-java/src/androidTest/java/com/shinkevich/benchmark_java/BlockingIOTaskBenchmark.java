package com.shinkevich.benchmark_java;

import androidx.benchmark.BenchmarkState;
import androidx.benchmark.junit4.BenchmarkRule;

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
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(value = Parameterized.class)
public class BlockingIOTaskBenchmark {
    private static final String TAG = BlockingIOTaskBenchmark.class.getSimpleName();

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    @Parameterized.Parameter
    public int tasksNum;

    @Parameterized.Parameters
    public static Collection<Object[]> tasksNumValues() {
        return Arrays.asList(new Object[][]{{5}, {10}, {15}, {20}, {25}, {30}, {35}, {40}, {45}, {50}});
    }

    private Runnable blockingTask = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @Test
    public void cachedThreadPoolExecutor() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            List<Future> futures = new ArrayList(tasksNum);
            for (int i = 0; i < tasksNum; i++) {
                futures.add(executor.submit(blockingTask));
            }
            for (int i = 0; i < tasksNum; i++) {
                futures.get(i).get();
            }
        }
    }

    @Test
    public void rxJavaSchedulerIO() {
        Scheduler scheduler = Schedulers.io();
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            List<CompletableSource> resultObservables = new ArrayList<>(tasksNum);
            for (int i = 0; i < tasksNum; i++) {
                resultObservables.add(i, Completable.fromRunnable(blockingTask).subscribeOn(scheduler));
            }
            Completable
                    .merge(resultObservables)
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingAwait();
        }
    }
}
