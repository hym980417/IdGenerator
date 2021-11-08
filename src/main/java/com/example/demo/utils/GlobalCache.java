package com.example.demo.utils;

/**
 * @author ming
 * @date 2021/11/6 16:32
 */
public interface GlobalCache {
    /**
     * 将指定key，value缓存指定时间
     * @param key
     * @param value
     * @return
     */
    boolean set(String key,String value,Long cacheTime);

    /**
     * 将指定key，value缓存
     * @param key
     * @param value
     * @return
     */
    boolean set(String key,String value);

    /**
     * 获取key对应的value
     * @param key
     * @return
     */
    String get(String key);

}
