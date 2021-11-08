package com.example.demo.proxy;

/**
 * @author ming
 * @date 2021/11/7 18:11
 */
public interface CacheProxy {
    /**
     * 将指定key，value缓存指定时间,定期缓存
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(String key, String value, Long cacheTime, String cacheType);

    /**
     * if(A>B) then cache=A
     *
     * @param key
     * @param value
     * @param cacheType
     * @return
     */
    long cas(String key, String value, String cacheType);

    /**
     * 将指定key，value缓存,定期缓存
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(String key, String value, String cacheType);

    /**
     * 获取key对应的value
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 判断是否到了set或者get的时间
     *
     * @param key
     * @return
     */
    boolean isNeedGetOrSet(String key, String cacheType);

}
