package com.ikari.common.threadpool;

/**
 * @author chaodong.xi
 * @since 2018/10/8 22:47
 */
public class RunnableDenyException extends RuntimeException{

    public RunnableDenyException(String message) {
        super(message);
    }
}
