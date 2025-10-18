package com.cxy.reactor.demo;

public class Sleeps {
    public static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // just igore
        }
    }
}
