package com.ikari.common.threadpool;

/**
 * @author chaodong.xi
 * @since 2018/10/8 22:39
 */

@FunctionalInterface
public interface DenyPolicy {

    void reject(Runnable runnable, ThreadPool threadPool);

    class DiscardDenyPolicy implements DenyPolicy {

        @Override
        public void reject(Runnable runnable, ThreadPool threadPool) {

        }
    }

    class AbortDenyPolicy implements DenyPolicy {

        @Override
        public void reject(Runnable runnable, ThreadPool threadPool) {
            throw new RunnableDenyException("the runnable " + runnable + "will be abort");
        }
    }

    class RunnerDenyPolicy implements DenyPolicy {

        @Override
        public void reject(Runnable runnable, ThreadPool threadPool) {
            if (threadPool.isLive()) {
                runnable.run();
            }
        }
    }

}
