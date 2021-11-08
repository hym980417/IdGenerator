package com.example.demo.strategy.impl.single;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式自增ID雪花算法snowflake，自旋解决时钟回拨问题
 *
 * @author ming
 * @date 2021/11/5 22:59
 */
@Component("SpinSnowFlakeStrategyForSingleMode")
@Slf4j
public class SpinSnowFlakeStrategyForSingleMode extends RejectSnowFlakeStrategyForSingleMode {

    /**
     * 时间回拨容忍的上限
     */
    private static long threshold = 1000;


    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long getNextId(String increaseType) {
        long currStmp = getNewstmp();
        //时间是否回拨
        boolean isClockBackwards = lastStmp > currStmp;

        if (isClockBackwards) {
            //时间回拨，尝试自旋解决
            currStmp = getNextMill(lastStmp - currStmp, threshold);
        }
        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            long step = getStepByType(increaseType);
            //同一毫秒的序列数已经达到最大
            if (sequence >= MAX_SEQUENCE) {
                currStmp = getNextMill(1L);
            }
            compareAndSwap(currStmp, step);
        } else {
            //不同毫秒内，序列号置为0
            compareAndSwap(currStmp, 0L);
        }

        return generateId(currStmp);
    }


    /**
     * 自旋获取下一个有效的时间戳
     *
     * @return
     */
    private long getNextMill(Long step, Long threshold) {
        if (step > threshold) {
            //时间回拨量超过阈值，系统异常，自旋时间长，抛出异常
            log.error("SpinSnowFlakeStrategyForSingleMode getNextMill: Clock moved backwards.  Refusing to generate id");
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }
        lastStmp = getLastStmp();
        try {
            TimeUnit.MICROSECONDS.sleep(step);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lastStmp = getLastStmp();
        long curStmp = getNewstmp();
        if (lastStmp > curStmp) {
            //递归调用
            getNextMill(lastStmp - curStmp, threshold - step);
        }
        return curStmp;
    }


}