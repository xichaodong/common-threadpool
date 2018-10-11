package com.ikari.common.threadpool;

import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author chaodong.xi
 * @since 2018/10/8 23:15
 */
public class LinkedRunnableQueue implements RunnableQueue {
    private final int limit;
    private final DenyPolicy denyPolicy;
    private final ThreadPool threadPool;
    private final static Object lock = new Object();
    private LinkedList<Runnable> runnableList = new LinkedList<>();

    public LinkedRunnableQueue(int limit, DenyPolicy denyPolicy, ThreadPool threadPool) {
        this.limit = limit;
        this.denyPolicy = denyPolicy;
        this.threadPool = threadPool;
    }

    @Override
    public void push(Runnable runnable) {
        synchronized (lock) {
            if (runnableList.size() >= limit) {
                denyPolicy.reject(runnable, threadPool);
            } else {
                runnableList.add(runnable);
                lock.notifyAll();
            }
        }
    }

    @Override
    public Runnable pop() throws InterruptedException {
        synchronized (lock) {
            while (CollectionUtils.isEmpty(runnableList)) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw e;
                }
            }
        }
        return runnableList.removeFirst();
    }

    @Override
    public int size() {
        synchronized (lock) {
            return runnableList.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return runnableList.isEmpty();
        }
    }
}
