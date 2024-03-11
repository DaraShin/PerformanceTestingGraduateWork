package com.shinkevich.benchmark_kotlin

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shinkevich.benchmarkable.DBTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class AsyncDBSingleBenchmark {
    @get:Rule
    var benchmarkRule = BenchmarkRule()

    @Test
    fun suspendTest(){
        val dbTest = DBTest(InstrumentationRegistry.getInstrumentation().context)
        val personDao = dbTest.personDao
        benchmarkRule.measureRepeated {
            runWithTimingDisabled { dbTest.initDatabase() }
            runBlocking(Dispatchers.Main) {
                CoroutineScope(Dispatchers.IO).async {
                    personDao.getByNameStartSuspendable("a")
                }.await()
            }
        }
    }

    @Test
    fun flowTest(){
        val dbTest = DBTest(InstrumentationRegistry.getInstrumentation().context)
        val personDao = dbTest.personDao
        benchmarkRule.measureRepeated {
            runWithTimingDisabled { dbTest.initDatabase() }
            runBlocking(Dispatchers.Main) {
               personDao
                   .getByNameStartFlow("a")
                   .take(1)
                   .flowOn(Dispatchers.IO)
                   .collect()
            }
        }
    }

    companion object {
        private val TAG = AsyncDBSingleBenchmark::class.java.simpleName
    }
}
