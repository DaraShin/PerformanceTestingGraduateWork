package com.shinkevich.benchmark_kotlin

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class BlockingIOTaskBenchmark(
    private val tasksNum: Int
) {
    @get:Rule
    var benchmarkRule = BenchmarkRule()

    @Test
    fun coroutinesDispatcherIOSleep() {
        val dispatcher = Dispatchers.IO
        benchmarkRule.measureRepeated {
            runBlocking {
                val jobList = mutableListOf<Job>()
                for (i in 1..tasksNum) {
                    jobList.add(CoroutineScope(dispatcher).launch {
                        Thread.sleep(20)
                    })
                }
                jobList.joinAll()
            }
        }
    }

    @Test
    fun flowsDispatcherIOSleep() {
        val dispatcher = Dispatchers.IO
        benchmarkRule.measureRepeated {
            runBlocking {
                val flowList = mutableListOf<Flow<Unit>>()
                for (i in 1..tasksNum) {
                    flowList.add(flow {
                        Thread.sleep(20)
                    })
                }
                val result = flowList
                    .merge()
                    .flowOn(dispatcher)
                    .collect()
            }
        }
    }

    companion object {
        private val TAG = BlockingIOTaskBenchmark::class.java.simpleName

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return arrayListOf(
                arrayOf(5),
                arrayOf(10),
                arrayOf(15),
                arrayOf(20),
                arrayOf(25),
                arrayOf(30),
                arrayOf(35),
                arrayOf(40),
                arrayOf(45),
                arrayOf(50)
            ).toList() as Iterable<Array<Any>>
        }
    }
}
