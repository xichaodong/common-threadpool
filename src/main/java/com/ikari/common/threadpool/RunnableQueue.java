package com.ikari.common.threadpool;

/**
 * @author chaodong.xi
 * @since 2018/10/8 22:33
 */
public interface RunnableQueue {

    void push(Runnable runnable);

    Runnable pop() throws InterruptedException;

    int size();

    boolean isEmpty();
}
