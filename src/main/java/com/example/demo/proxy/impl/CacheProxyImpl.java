package com.example.demo.proxy.impl;

import com.example.demo.constants.CacheFrequency;
import com.example.demo.proxy.CacheProxy;
import com.example.demo.utils.GlobalCache;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * 缓存代理，实现一些自定义特性，如定期缓存
 *
 * @author ming
 * @date 2021/11/7 18:13
 */
@Service
@Slf4j
public class CacheProxyImpl implements CacheProxy {

    @Autowired
    private GlobalCache globalCache;

    private static final Map<String, Long> cacheTimeMap = new HashMap<>();


    @Override
    public boolean set(String key, String value, Long cacheTime, String cacheType) {
        Long curTime = System.currentTimeMillis();
        if (isNeedGetOrSet(key, cacheType)) {
            cacheTimeMap.put(cacheType, curTime);
            return globalCache.set(key, value, cacheTime);
        }
        return true;
    }

    /**
     * 此处因时间限制，并未真正实现cas
     *
     * @param key
     * @param value
     * @param cacheType
     * @return
     */
    @Override
    public long cas(String key, String value, String cacheType) {
        long stmp = Long.parseLong(value);
        if (!isNeedGetOrSet(key, cacheType)) {
            return stmp;
        }
        String lastStmpCacheStr = globalCache.get(key);

        if (!StringUtil.isBlank(lastStmpCacheStr)) {
            Long lastStmpCache = Long.parseLong(lastStmpCacheStr);
            if (lastStmpCache < stmp) {
                globalCache.set(key, String.valueOf(stmp).trim());
            }
        }
        return stmp;
    }


    @Override
    public boolean set(String key, String value, String cacheType) {
        if (isNeedGetOrSet(key, cacheType)) {
            cacheTimeMap.put(cacheType, System.currentTimeMillis());
            return globalCache.set(key, value);
        }
        return true;
    }

    @Override
    public String get(String key) {
        return globalCache.get(key);
    }

    @Override
    public boolean isNeedGetOrSet(String key, String cacheType) {
        if (CacheFrequency.cacheFrequencyMap.get(cacheType) == null) {
            log.warn("CacheProxyImpl isNeedGetOrSet(): can not match input cache type");
            return false;
        }
        Long lastCacheTime = cacheTimeMap.get(cacheType);
        Long curTime = System.currentTimeMillis();
        if (lastCacheTime == null || (curTime - lastCacheTime) > CacheFrequency.cacheFrequencyMap.get(cacheType)) {
            return true;
        }
        return false;
    }
}
