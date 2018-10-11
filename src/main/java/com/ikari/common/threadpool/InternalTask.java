package com.ikari.common.threadpool;

/**
 * @author chaodong.xi
 * @since 2018/10/8 22:57
 */
public class InternalTask implements Runnable {

    private final RunnableQueue runnableQueue;

    private volatile boolean running = true;

    public InternalTask(RunnableQueue runnableQueue) {
        this.runnableQueue = runnableQueue;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {

            try {
                Runnable task = runnableQueue.pop();
                task.run();
            }catch (InterruptedException e){
                running = false;
                break;
            }
        }
    }

    public void stop(){
        running = false;
    }
}
