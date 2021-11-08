package com.example.demo.utils.impl;

import com.example.demo.utils.GlobalLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author ming
 * @date 2021/11/6 19:25
 */
@Service
public class RedisLock implements GlobalLock {
    @Autowired
    RedissonClient redissonClient;

    @Override
    public boolean lock(String key, Long lockTime) {
        RLock redLock = redissonClient.getLock(key);
        boolean isLock = false;
        try {
            isLock = redLock.tryLock(0, lockTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
        }
        return isLock;
    }

    @Override
    public void unLock(String key) {
        RLock redLock = redissonClient.getLock(key);
        try {
            redLock.unlock();
        }catch (Exception e){

        }
    }

    @Override
    public Long isLock(String key) {
        RLock redLock = redissonClient.getLock(key);
        return redLock.remainTimeToLive();
    }
}
