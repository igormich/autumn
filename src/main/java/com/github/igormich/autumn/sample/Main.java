package com.github.igormich.autumn.sample;

import com.github.igormich.autumn.Application;
import com.github.igormich.autumn.AutoWired;
import com.github.igormich.autumn.Autumn;

public class Main implements Application {
    @AutoWired
    private IntBiFunction function;

    public static void main(String[] args) throws Exception {
        Autumn.start(Main.class);
    }

    @Override
    public void run() {
        System.out.println(function.apply(40,2));;
    }
}