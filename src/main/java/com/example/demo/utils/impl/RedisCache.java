package com.example.demo.utils.impl;

import com.example.demo.utils.GlobalCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author ming
 * @date 2021/11/6 19:26
 */
@Service
public class RedisCache implements GlobalCache {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;



    @Override
    public boolean set(String key, String value, Long cacheTime) {
        redisTemplate.opsForValue().set(key,value,cacheTime);
        return true;
    }

    @Override
    public boolean set(String key, String value) {
        redisTemplate.opsForValue().set(key,value);
        return true;
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

}
