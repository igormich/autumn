package com.github.igormich.autumn.sample;

import com.github.igormich.autumn.PrintToConsole;

public class IntSumFunction implements IntBiFunction {
    @Override
    @PrintToConsole
    public int apply(int a, int b) {
        return a + b;
    }
}
