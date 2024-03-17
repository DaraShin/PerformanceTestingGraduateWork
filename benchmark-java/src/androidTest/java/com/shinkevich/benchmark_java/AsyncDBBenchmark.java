package com.shinkevich.benchmark_java;

import androidx.benchmark.BenchmarkState;
import androidx.benchmark.junit4.BenchmarkRule;
import androidx.lifecycle.Observer;
import androidx.lifecycle.testing.TestLifecycleOwner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.shinkevich.benchmarkable.DBTest;
import com.shinkevich.benchmarkable.Person;
import com.shinkevich.benchmarkable.PersonDao;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(value = Parameterized.class)
public class AsyncDBBenchmark {
    private static final String TAG = AsyncDBBenchmark.class.getSimpleName();
    private static final Integer ALPHABET_SIZE = 26;

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    @Parameterized.Parameter
    public int tasksNum;

    @Parameterized.Parameters
    public static Collection<Object[]> tasksNumValues() {
        return Arrays.asList(new Object[][]{
                {5}, {10}, {15}, {20}, {25}, {30}, {35}, {40}, {45}, {50}});
    }

    @Test
    public void rxJavaSingleTest() {
        BenchmarkState state = benchmarkRule.getState();
        DBTest dbTest = new DBTest(InstrumentationRegistry.getInstrumentation().getContext());
        PersonDao personDao = dbTest.getPersonDao();
        while (state.keepRunning()) {
            benchmarkRule.getScope().runWithTimingDisabled(() -> {
                dbTest.initDatabase();
                return null;
            });
            Observable
                    .range(0, tasksNum)
                    .flatMapSingle(taskNum -> personDao.getByNameStartSingle(getNameStartForSearch(taskNum)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe();
        }
    }

    @Test
    public void rxJavaObservableTest() {
        BenchmarkState state = benchmarkRule.getState();
        DBTest dbTest = new DBTest(InstrumentationRegistry.getInstrumentation().getContext());
        PersonDao personDao = dbTest.getPersonDao();
        while (state.keepRunning()) {
            benchmarkRule.getScope().runWithTimingDisabled(() -> {
                dbTest.initDatabase();
                return null;
            });
            Observable
                    .range(0, tasksNum)
                    .flatMap(taskNum -> personDao.getByNameStartObservable(getNameStartForSearch(taskNum)).take(1))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe();
        }
    }

    @Test
    public void executorTest() throws ExecutionException, InterruptedException {
        BenchmarkState state = benchmarkRule.getState();
        DBTest dbTest = new DBTest(InstrumentationRegistry.getInstrumentation().getContext());
        PersonDao personDao = dbTest.getPersonDao();
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (state.keepRunning()) {
            benchmarkRule.getScope().runWithTimingDisabled(() -> {
                dbTest.initDatabase();
                return null;
            });
            List<Future<List<Person>>> results = new ArrayList<>();
            for (int i = 0; i < tasksNum; i++) {
                int finalI = i;
                results.add(executorService.submit(() -> personDao.getByNameStart(getNameStartForSearch(finalI))));
            }
            for (Future<List<Person>> result : results) {
                result.get();
            }
        }
    }

//    @Test
//    public void liveDataTest() throws InterruptedException {
//        BenchmarkState state = benchmarkRule.getState();
//        DBTest dbTest = new DBTest(InstrumentationRegistry.getInstrumentation().getContext());
//        PersonDao personDao = dbTest.getPersonDao();
//        CountDownLatch countDownLatch = new CountDownLatch(tasksNum);
//        while (state.keepRunning()) {
//            benchmarkRule.getScope().runWithTimingDisabled(() -> {
//                dbTest.initDatabase();
//                return null;
//            });
//            for (int i = 0; i < tasksNum; i++) {
//                personDao.getByNameStartLiveData("a").observe(new TestLifecycleOwner(), new Observer<List<Person>>() {
//                    @Override
//                    public void onChanged(List<Person> people) {
//                        countDownLatch.countDown();
//                    }
//                });
//            }
//            countDownLatch.await();
//        }
//    }

    private String getNameStartForSearch(Integer taskNum) {
        return Character.toString((char) (taskNum % ALPHABET_SIZE + 'a'));
    }
}