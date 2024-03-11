package com.shinkevich.benchmark_kotlin

object PiCalculator {
    private const val START_X = 0.0
    private const val END_X = 1.0
    fun calculatePiThreadTask(threadNum: Int, stepsNum: Int, totalThreadsNum: Int): Double {
        var result = 0.0
        var currentX = START_X + (END_X - START_X) / stepsNum * threadNum
        val step = (END_X - START_X) / stepsNum * totalThreadsNum
        while (currentX < END_X) {
            result += 1.0 / (1.0 + currentX * currentX)
            currentX += step
        }
        return result * (END_X - START_X) / stepsNum * 4.0
    }

    fun calculateIntervalValue(stepNum: Int, stepsNum: Int): Double {
        val step = (END_X - START_X) / stepsNum
        val currentX = START_X + stepNum * step
        return 1.0 / (1.0 + currentX * currentX) * step * 4.0
    }
}
