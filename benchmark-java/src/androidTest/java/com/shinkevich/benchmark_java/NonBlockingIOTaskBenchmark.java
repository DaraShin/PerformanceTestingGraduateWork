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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(value = Parameterized.class)
public class NonBlockingIOTaskBenchmark {
    private static final String TAG = NonBlockingIOTaskBenchmark.class.getSimpleName();
    private AtomicInteger atomicInt = new AtomicInteger(0);

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    @Parameterized.Parameter
    public int tasksNum;

    @Parameterized.Parameters
    public static Collection<Object[]> tasksNumValues() {
        return Arrays.asList(new Object[][]{
                        {64}, {128}, {256},{512}, {1024}
                }
        );
    }

    @Test
    public void cachedThreadPoolExecutor() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            List<Future> futures = new ArrayList(tasksNum);
            for(int i = 0; i < tasksNum; i++) {
                futures.add(executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            nonBlockingTask().get();
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }
            for(int i = 0; i < tasksNum; i++) {
                futures.get(i).get();
            }
        }
    }

    @Test
    public void rxJavaSchedulerIO() throws ExecutionException, InterruptedException {
        Scheduler scheduler = Schedulers.io();
        BenchmarkState state = benchmarkRule.getState();
        while (state.keepRunning()) {
            List<CompletableSource> resultObservables = new ArrayList<>(tasksNum);
            for(int i = 0; i < tasksNum; i++){
                resultObservables.add(i, Completable.fromRunnable(() -> {
                    try {
                        nonBlockingTask().get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).subscribeOn(scheduler));
            }
            Completable
                    .merge((Iterable<CompletableSource>)resultObservables)
                    //.subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingAwait();
        }
    }

    private Future<Integer> nonBlockingTask(){
        return Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(10);
                return atomicInt.incrementAndGet();
            }
        });
    }
}
