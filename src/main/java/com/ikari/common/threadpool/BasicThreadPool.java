package com.ikari.common.threadpool;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author chaodong.xi
 * @since 2018/10/10 20:38
 */
public class BasicThreadPool extends Thread implements ThreadPool {
    private final int initSize;
    private final int maxSize;
    private final int coreSize;
    private volatile int activeCount;
    private final ThreadFactory threadFactory;
    private RunnableQueue runnableQueue;
    private volatile boolean isShutdown = false;
    private final Queue<ThreadTask> threadQueue = new ArrayDeque<>();
    private final static DenyPolicy DEFAULT_DENY_POLICY = new DenyPolicy.DiscardDenyPolicy();
    private final static ThreadFactory DEFAULT_THREAD_FACTORY = new DefaultThreadFactory();
    private final long keepAliveTime;
    private final TimeUnit timeUnit;

    public BasicThreadPool(int initSize, int maxSize, int coreSize, int queueSize) {
        this(initSize, maxSize, coreSize, DEFAULT_THREAD_FACTORY,
                10, queueSize, DEFAULT_DENY_POLICY, TimeUnit.SECONDS);
    }

    public BasicThreadPool(int initSize, int maxSize, int coreSize,
                           ThreadFactory threadFactory, long keepAliveTime, int queueSize,
                           DenyPolicy denyPolicy, TimeUnit timeUnit) {
        this.initSize = initSize;
        this.maxSize = maxSize;
        this.coreSize = coreSize;
        this.threadFactory = threadFactory;
        this.runnableQueue = new LinkedRunnableQueue(queueSize, denyPolicy, this);
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        init();
    }

    private void init() {
        start();
        IntStream.range(0, initSize).forEach(number -> newThread());
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger GROUP_COUNTER = new AtomicInteger(1);

        private static final ThreadGroup THREAD_GROUP = new ThreadGroup("MyThreadPool-" + GROUP_COUNTER.getAndDecrement());

        private static final AtomicInteger COUNTER = new AtomicInteger(0);

        @Override
        public Thread createThread(Runnable runnable) {
            return new Thread(THREAD_GROUP, runnable, "thread-pool-" + COUNTER.getAndDecrement());
        }
    }

    private static class ThreadTask {
        public ThreadTask(Thread thread, InternalTask internalTask) {
            this.thread = thread;
            this.internalTask = internalTask;
        }

        Thread thread;
        InternalTask internalTask;
    }

    private void newThread() {
        InternalTask internalTask = new InternalTask(runnableQueue);
        Thread thread = threadFactory.createThread(internalTask);
        ThreadTask threadTask = new ThreadTask(thread, internalTask);
        threadQueue.offer(threadTask);
        activeCount++;
        thread.start();
    }

    private void removeThread() {
        ThreadTask threadTask = threadQueue.remove();
        threadTask.internalTask.stop();
        activeCount--;
    }

    @Override
    public void run() {
        while (!isShutdown && !isInterrupted()) {
            try {
                timeUnit.sleep(keepAliveTime);
            } catch (InterruptedException e) {
                isShutdown = true;
                break;
            }

            synchronized (this) {
                if (isShutdown) {
                    break;
                }

                if (runnableQueue.size() > 0 && activeCount < coreSize) {
                    IntStream.range(initSize, coreSize).forEach(number -> newThread());
                    continue;
                }

                if (runnableQueue.size() > 0 && activeCount < maxSize) {
                    IntStream.range(coreSize, maxSize).forEach(number -> newThread());
                }

                if (runnableQueue.size() == 0 && activeCount > coreSize) {
                    IntStream.range(coreSize, activeCount).forEach(number -> removeThread());
                }
            }
        }
    }

    @Override
    public void submit(Runnable runnable) {
        if (isShutdown) {
            throw new IllegalStateException("The thread pool is destroy");
        }

        runnableQueue.push(runnable);
    }

    @Override
    public void shutdown() {
        synchronized (this) {
            if (!isShutdown) {
                return;
            }

            isShutdown = true;
            threadQueue.forEach(threadTask -> {
                threadTask.internalTask.stop();
                threadTask.thread.interrupt();
            });
            interrupt();
        }
    }

    @Override
    public int getInitSize() {
        if (isShutdown) {
            throw new IllegalStateException("The thread pool is destroy");
        }
        return initSize;
    }

    @Override
    public int getMaxSize() {
        if (isShutdown) {
            throw new IllegalStateException("The thread pool is destroy");
        }
        return maxSize;
    }

    @Override
    public int getCoreSize() {
        if (isShutdown) {
            throw new IllegalStateException("The thread pool is destroy");
        }
        return coreSize;
    }

    @Override
    public int getQueueSize() {
        if (isShutdown) {
            throw new IllegalStateException("The thread pool is destroy");
        }
        return runnableQueue.size();
    }

    @Override
    public int getActiveCount() {
        synchronized (this) {
            return this.activeCount;
        }
    }

    @Override
    public boolean isLive() {
        return !isShutdown;
    }
}
