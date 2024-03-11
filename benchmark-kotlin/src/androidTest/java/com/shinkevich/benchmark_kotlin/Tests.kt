package com.shinkevich.benchmark_kotlin

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import kotlin.math.abs

object Tests {
    fun coroutinesTest(benchmarkRule: BenchmarkRule, stepsNum: Int, precision: Double, threadsNum: Int, dispatcher: CoroutineDispatcher){
        benchmarkRule.measureRepeated {
            runBlocking {
                val jobList = mutableListOf<Deferred<Double>>()
                for (i in 0 until threadsNum) {
                    jobList.add(i, CoroutineScope(dispatcher).async {
                        PiCalculator.calculatePiThreadTask(i, stepsNum, threadsNum)
                    })
                }

                val result = jobList.fold(0.0) { result, job -> result + job.await() }
                runWithTimingDisabled {
                    Assert.assertTrue(abs(result - Math.PI) < precision)
                }
            }
        }
    }

    fun coroutinesTest(benchmarkRule: BenchmarkRule, stepsNum: Int, precision: Double, threadsNum: Int){
        benchmarkRule.measureRepeated {
            val dispatcher = Dispatchers.Default.limitedParallelism(threadsNum)
            runBlocking {
                val jobList = mutableListOf<Deferred<Double>>()
                for (i in 0 until threadsNum) {
                    jobList.add(i, CoroutineScope(dispatcher).async {
                        PiCalculator.calculatePiThreadTask(i, stepsNum, threadsNum)
                    })
                }

                val result = jobList.fold(0.0) { result, job -> result + job.await() }
                runWithTimingDisabled {
                    Assert.assertTrue(abs(result - Math.PI) < precision)
                }
            }
        }
    }

    fun flowTest(benchmarkRule: BenchmarkRule, stepsNum: Int, precision: Double, threadsNum: Int, dispatcher: CoroutineDispatcher){
        benchmarkRule.measureRepeated {
            runBlocking {
                val flowList = mutableListOf<Flow<Double>>()
                for (i in 0 until threadsNum) {
                    flowList.add(i, flow {
                        emit(PiCalculator.calculatePiThreadTask(i, stepsNum, threadsNum))
                    })
                }
                val result = flowList
                    .merge()
                    .flowOn(dispatcher)
                    .fold(0.0) { result, value -> result + value }
                runWithTimingDisabled {
                    Assert.assertTrue(abs(result - Math.PI) < precision)
                }
            }
        }
    }

    fun flowTest(benchmarkRule: BenchmarkRule, stepsNum: Int, precision: Double, threadsNum: Int){
        benchmarkRule.measureRepeated {
            val dispatcher = Dispatchers.Default.limitedParallelism(threadsNum)
            runBlocking {
                val flowList = mutableListOf<Flow<Double>>()
                for (i in 0 until threadsNum) {
                    flowList.add(i, flow {
                        emit(PiCalculator.calculatePiThreadTask(i, stepsNum, threadsNum))
                    })
                }
                val result = flowList
                    .merge()
                    .flowOn(dispatcher)
                    .fold(0.0) { result, value -> result + value }
                runWithTimingDisabled {
                    Assert.assertTrue(abs(result - Math.PI) < precision)
                }
            }
        }
    }

    fun getPrecision(stepsNum: Int): Double {
        return 10.0 / stepsNum
    }
}