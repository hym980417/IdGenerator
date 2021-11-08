package com.example.demo.utils;

/**
 * @author ming
 * @date 2021/11/6 16:31
 */
public interface GlobalLock {
    /**
     * 将指定key加指定时间的锁
     * @param key
     * @param lockTime
     * @return
     */
    boolean lock(String key,Long lockTime);

    /**
     * 为指定key解锁
     * @param key
     * @return
     */
    void unLock(String key);

    /**
     * 判断指定key是否有锁，有锁的情况下返回锁剩余时间（ms），无锁返回0
     * @param key
     * @return
     */
    Long isLock(String key);
}
