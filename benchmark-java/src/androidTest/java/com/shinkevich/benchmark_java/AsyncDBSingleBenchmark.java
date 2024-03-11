package com.shinkevich.benchmark_java;

import androidx.benchmark.BenchmarkState;
import androidx.benchmark.junit4.BenchmarkRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.shinkevich.benchmarkable.DBTest;
import com.shinkevich.benchmarkable.PersonDao;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(value = AndroidJUnit4.class)
public class AsyncDBSingleBenchmark {
    private static final String TAG = AsyncDBSingleBenchmark.class.getSimpleName();

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();

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
            personDao.getByNameStartSingle("a")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingGet();
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
            personDao.getByNameStartObservable("a")
                    .take(1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe();
        }
    }
}