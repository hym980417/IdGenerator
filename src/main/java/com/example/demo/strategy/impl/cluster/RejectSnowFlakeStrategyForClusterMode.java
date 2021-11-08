package com.example.demo.strategy.impl.cluster;

import com.example.demo.constants.IdGenerateType;
import com.example.demo.strategy.AbstractSnowFlakeStrategy;
import com.example.demo.utils.GlobalCache;
import com.example.demo.utils.GlobalLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 分布式自增ID雪花算法snowflake,集群版本，使用拒绝策略解决时间回拨问题,可能会导致服务不可用
 *
 * @author ming
 * @date 2021/11/5 22:59
 */
@Component("RejectSnowFlakeStrategyForClusterMode")
@Slf4j
public class RejectSnowFlakeStrategyForClusterMode extends AbstractSnowFlakeStrategy {

    @Autowired
    protected GlobalLock globalLock;

    @Autowired
    protected GlobalCache globalCache;

    /**
     * 服务类型
     */
    protected boolean isCluster = true;

    /**
     * 加锁时key的前缀
     */
    protected static final String LOCK_PREFIX = "LOCK_CLUSTER";

    /**
     * 缓存时key的前缀
     */
    protected static final String CACHE_PREFIX = "CACHE_CLUSTER";


    /**
     * 产生下一个ID
     *
     * @return
     */
    @Override
    public synchronized long getNextId(String increaseType) {
        lastStmp = getLastStmp();
        long currStmp = getNewstmp();
        //时间是否回拨
        boolean isClockBackwards = lastStmp > currStmp;
        if (isClockBackwards) {
            //时间回拨系统异常，抛出异常
            log.error("Clock moved backwards.  Refusing to generate id");
            return -1L;
        }

        long step = getStepByType(increaseType);
        compareAndSwap(currStmp, step);

        return generateId(currStmp);
    }

    /**
     * 将当前时间戳对应的sequence取出递增后再加入缓存
     *
     * @param stmp
     * @param step
     */
    @Override
    protected void compareAndSwap(long stmp, long step) {
        if (stmp <= 0L) {
            log.error("RejectSnowFlakeStrategyForClusterMode compareAndSwap() : time stamp error");
            return;
        }
        Long key = ((stmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT    //数据中心部分
                | machineId << MACHINE_LEFT);        //机器标识部分
        String keyString = key.toString().trim();
        String lockKey = LOCK_PREFIX + keyString;
        String cacheKey = CACHE_PREFIX + keyString;
        try {
            globalLock.lock(lockKey, 100L);
            String cachedSequenceString = globalCache.get(cacheKey);
            if (cachedSequenceString != null) {
                sequence = Long.parseLong(cachedSequenceString.trim());
                if (sequence > MAX_SEQUENCE) {
                    compareAndSwap(stmp + 1, step);
                    return;
                }
            }
            globalCache.set(cacheKey, String.valueOf(sequence + step), 3000L);
        } finally {
            globalLock.unLock(lockKey);
        }

        //将最新的（且需要是最大）的时间戳存到缓存里
        cacheProxy.cas(CACHE_PREFIX + LAST_STMP_STR, String.valueOf(stmp).trim(), IdGenerateType.RejectSnowFlakeStrategyForSingleModeType);
        //更新本地时间戳
        lastStmp = stmp;

    }


}