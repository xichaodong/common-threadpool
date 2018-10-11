package com.ikari.common.threadpool;

/**
 * @author chaodong.xi
 * @since 2018/10/8 22:16
 */
public interface ThreadPool {

    void submit(Runnable runnable);

    void shutdown();

    int getInitSize();

    int getMaxSize();

    int getCoreSize();

    int getQueueSize();

    int getActiveCount();

    boolean isLive();
}
