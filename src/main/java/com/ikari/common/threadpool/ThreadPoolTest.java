package com.ikari.common.threadpool;

import java.util.concurrent.TimeUnit;

/**
 * @author chaodong.xi
 * @since 2018/10/10 23:54
 */
public class ThreadPoolTest {
    public static void main(String[] args) throws InterruptedException {
        ThreadPool threadPool = new BasicThreadPool(2, 6, 4, 1000);

        for (int i = 0; i < 200; i++) {
            threadPool.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(Thread.currentThread().getName() + " is running and done.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        while (true) {
            System.out.println("ActivitySize = " + threadPool.getActiveCount());
            System.out.println("QueueSize = " + threadPool.getQueueSize());
            System.out.println("CoreSize = " + threadPool.getCoreSize());
            System.out.println("MaxSize = " + threadPool.getMaxSize());
            System.out.println("=============================================");
            TimeUnit.SECONDS.sleep(1);
        }

    }
}
