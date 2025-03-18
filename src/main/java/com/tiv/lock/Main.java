package com.tiv.lock;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        final int[] count = {1000};
        List<Thread> threads = new ArrayList<>();
        MyLock lock = new MyLock();

        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(() -> {
                lock.lock();
                for (int j = 0; j < 10; j++) {
                    count[0]--;
                }
                lock.unlock();
            }));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(count[0]);
    }
}
