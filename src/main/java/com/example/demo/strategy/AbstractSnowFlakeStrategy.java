package com.example.demo.strategy;

import com.example.demo.constants.IdGenerateType;
import com.example.demo.proxy.CacheProxy;
import com.example.demo.utils.GlobalLock;
import com.google.common.base.Preconditions;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ming
 * @date 2021/11/7 21:04
 */
@Component
@Slf4j
public abstract class AbstractSnowFlakeStrategy implements IdGenerateStrategy {

    @Autowired
    protected GlobalLock globalLock;

    @Autowired
    protected CacheProxy cacheProxy;

    /**
     * 服务类型
     */
    protected boolean isCluster = false;

    /**
     * 起始的时间戳
     */
    protected final static long START_STMP = 1480166465631L;

    /**
     * 步长增长类型
     */
    public final static String REGULAR_INCREASE_TYPE = "REGULAR";//步长有规律的增长
    public final static String IRREGULAR_INCREASE_TYPE = "IRREGULAR";//步长无规律的增长

    /**
     * 步长
     */
    protected final static Long STEP = 1L;
    protected final static Long DEFAULT_STEP = 1L;
    /**
     * 每一部分占用的位数
     */
    @Value("${snowflake.bit.machine}")
    protected long MACHINE_BIT;   //机器标识占用的位数
    @Value("${snowflake.bit.datacenter}")
    protected long DATACENTER_BIT;//数据中心占用的位数
    protected long SEQUENCE_BIT = 22 - MACHINE_BIT - DATACENTER_BIT; //序列号占用的位数

    /**
     * 每一部分的最大值
     */
    protected  long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    protected  long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    protected  long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 存储上一次的使用的时间戳的key
     */
    protected final static String LAST_STMP_STR = "lastStmp";
    /**
     * 加锁时key的前缀
     */
    protected static final String LOCK_PREFIX = "LOCK_SINGLE";

    /**
     * 缓存时key的前缀
     */
    protected static final String CACHE_PREFIX = "CACHE_SINGLE";

    /**
     * 每一部分向左的位移
     */
    protected  long MACHINE_LEFT = SEQUENCE_BIT;
    protected  long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    protected  long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    protected long datacenterId;  //数据中心
    protected long machineId;     //机器标识
    protected long sequence = 0L; //序列号
    protected long lastStmp = -1L;//上一次时间戳


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
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
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
     * 生成id
     *
     * @param currStmp
     * @return
     */
    protected long generateId(long currStmp) {
        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    /**
     * 根据增长类型获取增长步长
     *
     * @param increaseType
     * @return
     */
    protected Long getStepByType(String increaseType) {
        if (Objects.equals(increaseType, REGULAR_INCREASE_TYPE)) {
            return STEP;
        } else if (Objects.equals(increaseType, IRREGULAR_INCREASE_TYPE)) {
            return (long) (1 + Math.random() * (10 - 1 + 1)) * STEP;
        }
        return DEFAULT_STEP;
    }

    protected void compareAndSwap(long stmp, long step) {
        //将最新的（且需要是最大）的时间戳存到缓存里
        cacheProxy.cas(CACHE_PREFIX + LAST_STMP_STR, String.valueOf(stmp).trim(), IdGenerateType.RejectSnowFlakeStrategyForSingleModeType);
        //更新本地时间戳
        lastStmp = stmp;
    }

    /**
     * 获取上一次保存的最大时间戳
     *
     * @return
     */
    protected long getLastStmp() {
        if (!cacheProxy.isNeedGetOrSet(CACHE_PREFIX + LAST_STMP_STR, IdGenerateType.RejectSnowFlakeStrategyForSingleModeType)) {
            return lastStmp;
        }
        long tmp;
        try {
            String value = cacheProxy.get(CACHE_PREFIX + LAST_STMP_STR);
            if (StringUtil.isBlank(value)) {
                lastStmp = getNewstmp();
            }
            tmp = Long.parseLong(value);
            lastStmp = Math.max(tmp, lastStmp);
        } catch (Exception e) {
            lastStmp = getNewstmp();
        }
        return lastStmp;
    }

    /**
     * 自旋获取下一个有效的时间戳
     *
     * @return
     */
    protected long getNextMill(Long step) {
        Preconditions.checkArgument(step < 0L, "AbstractSnowFlakeStrategy getNextMill(): step error");
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
     * 获取当前系统时间
     *
     * @return
     */
    protected long getNewstmp() {
        return System.currentTimeMillis();
    }


}
