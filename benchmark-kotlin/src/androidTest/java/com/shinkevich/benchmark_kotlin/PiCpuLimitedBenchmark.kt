package com.shinkevich.benchmark_kotlin

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(value = Parameterized::class)
class PiCpuLimitedBenchmark(
    private val stepsNum: Int
) {
    @get:Rule
    var benchmarkRule = BenchmarkRule()

    @Test
    fun coroutines_cpuLimitedThreads() {
        val processorsNum = Runtime.getRuntime().availableProcessors()
        Log.d(TAG, "cpu processors: $processorsNum")
        Tests.coroutinesTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), processorsNum, Dispatchers.Default.limitedParallelism(processorsNum))
    }

    @Test
    fun coroutines_cpuLimitedThreads_default() {
        val processorsNum = Runtime.getRuntime().availableProcessors()
        Log.d(TAG, "cpu processors: $processorsNum")
        Tests.coroutinesTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), processorsNum, Dispatchers.Default)
    }

    @Test
    fun flow_cpuLimitedThreads(){
        val processorsNum = Runtime.getRuntime().availableProcessors()
        Log.d(TAG, "cpu processors: $processorsNum")
        Tests.flowTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), processorsNum, Dispatchers.Default.limitedParallelism(processorsNum))
    }

    @Test
    fun flow_cpuLimitedThreads_default(){
        val processorsNum = Runtime.getRuntime().availableProcessors()
        Log.d(TAG, "cpu processors: $processorsNum")
        Tests.flowTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum), processorsNum, Dispatchers.Default)
    }

    companion object {
        private val TAG = PiCpuLimitedBenchmark::class.java.simpleName

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<Any>> {
            return arrayListOf(
                arrayOf(Math.pow(10.0, 4.0).toInt()),
                arrayOf(Math.pow(10.0, 5.0).toInt()),
                arrayOf(Math.pow(10.0, 6.0).toInt()),
                arrayOf(Math.pow(10.0, 7.0).toInt()),
                arrayOf(Math.pow(10.0, 8.0).toInt()),
            ).toList() as Iterable<Array<Any>>
        }
    }
}