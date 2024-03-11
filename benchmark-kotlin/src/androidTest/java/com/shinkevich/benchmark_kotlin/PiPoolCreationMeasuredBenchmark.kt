package com.shinkevich.benchmark_kotlin

import androidx.benchmark.junit4.BenchmarkRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Number of tasks = number of threads in pool.
 * Time of pool creation is measured.
 */
@RunWith(value = Parameterized::class)
class PiPoolCreationMeasuredBenchmark (
    private val stepsNum: Int
) {
    @get:Rule
    var benchmarkRule = BenchmarkRule()

    @Test
    fun coroutines_1thread(){
        Tests.coroutinesTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),1)
    }

    @Test
    fun coroutines_2threads(){
        Tests.coroutinesTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),2)
    }

    @Test
    fun coroutines_4threads(){
        Tests.coroutinesTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),4)
    }

    @Test
    fun coroutines_8threads(){
        Tests.coroutinesTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),8)
    }

    @Test
    fun flow_1Thread(){
        Tests.flowTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),1)
    }

    @Test
    fun flow_2Threads(){
        Tests.flowTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),2)
    }

    @Test
    fun flow_4Threads(){
        Tests.flowTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),4)
    }

    @Test
    fun flow_8Threads(){
        Tests.flowTest(benchmarkRule, stepsNum, Tests.getPrecision(stepsNum),8)
    }


    companion object {
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