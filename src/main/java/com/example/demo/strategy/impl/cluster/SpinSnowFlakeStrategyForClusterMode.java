package com.example.demo.strategy.impl.cluster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式自增ID雪花算法snowflake，自旋解决时钟回拨问题
 *
 * @author ming
 * @date 2021/11/5 22:59
 */
@Component("SpinSnowFlakeStrategyForClusterMode")
@Slf4j
public class SpinSnowFlakeStrategyForClusterMode extends RejectSnowFlakeStrategyForClusterMode {

    /**
     * 单次自旋时间量阈值
     */
    private long threshold = 1000;


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
            log.warn("SpinSnowFlakeStrategyForClusterMode getNextId() : Clock moved backwards.");
            currStmp = getNextMill(lastStmp - currStmp, threshold);
        }

        long step = getStepByType(increaseType);
        compareAndSwap(currStmp, step);

        return generateId(currStmp);
    }


    /**
     * 自旋获取下一个有效的时间戳
     *
     * @return
     */
    private long getNextMill(Long step, Long threshold) {
        if (step > threshold) {
            //时间回拨超过阈值，系统异常，自旋时间长，抛出异常
            log.error("SpinSnowFlakeStrategyForClusterMode getNextMill() : Clock moved backwards.  Refusing to generate id");
            throw new RuntimeException("SpinSnowFlakeStrategyForClusterMode getNextMill() : Clock moved backwards.  Refusing to generate id");
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