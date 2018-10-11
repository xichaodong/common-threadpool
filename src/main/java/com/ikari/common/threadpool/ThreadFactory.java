package com.ikari.common.threadpool;

/**
 * @author chaodong.xi
 * @since 2018/10/8 22:35
 */

@FunctionalInterface
public interface ThreadFactory {

    Thread createThread(Runnable runnable);
}
