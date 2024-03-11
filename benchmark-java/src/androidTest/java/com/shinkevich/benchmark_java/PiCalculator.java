package com.shinkevich.benchmark_java;

public class PiCalculator {
    static final double START_X  = 0.0;
    static final double END_X  = 1.0;
    static double calculatePiThreadTask(int threadNum, int stepsNum, int totalThreadsNum) {
        double result = 0;
        double currentX = START_X + (END_X - START_X) / stepsNum * threadNum;
        double step =  (END_X - START_X) / stepsNum * totalThreadsNum;
        while(currentX < END_X) {
            result += 1.0 / (1.0 + currentX * currentX);
            currentX += step;
        }
        return result * (END_X - START_X) / stepsNum * 4.0;
    }

    static double calculateIntervalValue(int stepNum, int stepsNum) {
        double step = (END_X - START_X) / stepsNum;
        double currentX = START_X + stepNum * step;
        return 1.0 / (1.0 + currentX * currentX) * step * 4.0;
    }
}
