package com.example.demo.strategy.impl.single;

import com.example.demo.strategy.AbstractSnowFlakeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 分布式自增ID雪花算法snowflake
 *
 * @author ming
 * @date 2021/11/5 22:59
 */
@Component("RejectSnowFlakeStrategyForSingleMode")
@Slf4j
public class RejectSnowFlakeStrategyForSingleMode extends AbstractSnowFlakeStrategy {

    /**
     * 服务类型
     */
    protected boolean isCluster = true;

    /**
     * 加锁时key的前缀
     */
    protected static final String LOCK_PREFIX = "LOCK_SINGLE";

    /**
     * 缓存时key的前缀
     */
    protected static final String CACHE_PREFIX = "CACHE_SINGLE";

    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long getNextId(String increaseType) {
        lastStmp = getLastStmp();
        long currStmp = getNewstmp();
        //时间是否回拨
        boolean isClockBackwards = lastStmp > currStmp;
        if (isClockBackwards) {
            //时间回拨，系统异常，抛出异常
            log.warn("RejectSnowFlakeStrategyForSingleMode getNextId() : Clock moved backwards. Refusing to generate id");
            throw new RuntimeException("RejectSnowFlakeStrategyForSingleMode getNextId() : Clock moved backwards. Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            long step = getStepByType(increaseType);
            //同一毫秒的序列数已经达到最大
            if (sequence >= MAX_SEQUENCE) {
                currStmp = getNextMill(1L);
            }
            sequence &= MAX_SEQUENCE - 1;
            compareAndSwap(currStmp, step);
        } else {
            //不同毫秒内，序列号置为0
            compareAndSwap(currStmp, 0L);
        }

        return generateId(currStmp);
    }

}