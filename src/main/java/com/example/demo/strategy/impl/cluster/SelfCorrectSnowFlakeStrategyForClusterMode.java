package com.example.demo.strategy.impl.cluster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 分布式自增ID雪花算法snowflake，使用offset自动校正解决时钟回拨问题
 *
 * @author ming
 * @date 2021/11/5 22:59
 */
@Component("SelfCorrectSnowFlakeStrategyForClusterMode")
@Slf4j
public class SelfCorrectSnowFlakeStrategyForClusterMode extends RejectSnowFlakeStrategyForClusterMode {


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
            log.warn("Clock moved backwards.  Self correct start!");
            currStmp = getAmendedStmp();
        }

        long step = getStepByType(increaseType);
        compareAndSwap(currStmp, step);

        return generateId(currStmp);
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