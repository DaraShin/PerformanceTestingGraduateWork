package com.shinkevich.benchmark_kotlin

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.platform.app.InstrumentationRegistry
import com.shinkevich.benchmarkable.DBTest
import com.shinkevich.benchmarkable.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class AsyncDBBenchmark(
    private val tasksNum: Int
) {
    @get:Rule
    var benchmarkRule = BenchmarkRule()

    @Test
    fun suspendTest() {
        val dbTest = DBTest(InstrumentationRegistry.getInstrumentation().context)
        val personDao = dbTest.personDao
        benchmarkRule.measureRepeated {
            runWithTimingDisabled { dbTest.initDatabase() }
            val results = mutableListOf<Deferred<List<Person>>>()
            runBlocking(Dispatchers.Main) {
                repeat(tasksNum) {
                    results.add(CoroutineScope(Dispatchers.IO).async {
                        personDao.getByNameStartSuspendable(getNameStartForSearch(it))
                    })
                }
                results.awaitAll()
            }
        }
    }

    @Test
    fun flowTest() {
        val dbTest = DBTest(InstrumentationRegistry.getInstrumentation().context)
        val personDao = dbTest.personDao
        benchmarkRule.measureRepeated {
            runWithTimingDisabled { dbTest.initDatabase() }
            runBlocking(Dispatchers.Main) {
                (0 until tasksNum).asFlow()
                    .flatMapMerge { taskNum ->
                        personDao.getByNameStartFlow(getNameStartForSearch(taskNum)).take(1)
                    }
                    .flowOn(Dispatchers.IO)
                    .collect()
            }
        }
    }

    @Test
    fun flowTest2() {
        val dbTest = DBTest(InstrumentationRegistry.getInstrumentation().context)
        val personDao = dbTest.personDao
        benchmarkRule.measureRepeated {
            runWithTimingDisabled { dbTest.initDatabase() }
            runBlocking(Dispatchers.Main) {
                val results = mutableListOf<Flow<List<Person>>>()
                for(taskNum in 0 until tasksNum){
                    results.add(personDao.getByNameStartFlow(getNameStartForSearch(taskNum)))
                }
                results
                    .merge()
                    .take(tasksNum)
                    .flowOn(Dispatchers.IO)
                    .collect()
            }
        }
    }

    private fun getNameStartForSearch(taskNum: Int): String {
        return (taskNum % ALPHABET_SIZE + 'a'.code).toChar().toString()
    }

    companion object {
        private val TAG = AsyncDBBenchmark::class.java.simpleName
        private const val ALPHABET_SIZE = 26

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
