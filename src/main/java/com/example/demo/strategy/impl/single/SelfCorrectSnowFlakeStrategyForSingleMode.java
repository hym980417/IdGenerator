package com.example.demo.strategy.impl.single;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式自增ID雪花算法snowflake，使用offset自动校正解决时钟回拨问题
 *
 * @author ming
 * @date 2021/11/5 22:59
 */
@Component("SelfCorrectSnowFlakeStrategyForSingleMode")
@Slf4j
public class SelfCorrectSnowFlakeStrategyForSingleMode extends RejectSnowFlakeStrategyForSingleMode {

    private long offset = 0;

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
            //时间回拨，使用offset进行修正
            currStmp = getAmendedStmp();
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
     * 自旋获取下一个有效的时间戳,有可能会无限等待
     *
     * @return
     */
    @Override
    protected long getNextMill(Long step) {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            try {
                TimeUnit.MICROSECONDS.sleep(step);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mill = getNewstmp();
        }
        return mill;
    }

    /**
     * 自动修正
     *
     * @return
     */
    private long getAmendedStmp() {
        long curStmp = getNewstmp();
        long realStmp = curStmp - offset;
        long result;
        if (realStmp >= lastStmp) {
            offset = 0;
            result = realStmp;
        } else {
            offset = lastStmp - realStmp;
            result = realStmp + offset;
        }
        return result;
    }

    /**
     * 获取时间时都用offset进行偏移修正
     *
     * @return
     */
    @Override
    protected long getNewstmp() {
        if (offset < 0) {
            log.warn("SelfCorrectSnowFlakeStrategyForClusterMode getNewstmp() : offset less than zero");
            offset = 0;
        }
        return System.currentTimeMillis() + offset;
    }

}