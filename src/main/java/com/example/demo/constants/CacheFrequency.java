package com.example.demo.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ming
 * @date 2021/11/7 19:52
 */
public class CacheFrequency {

    private static final long RejectSnowFlakeStrategyForClusterModeCacheFrequency = 1000L;

    private static final long SelfCorrectSnowFlakeStrategyForClusterModeCacheFrequency = 1000L;

    private static final long SpinSnowFlakeStrategyForClusterModeCacheFrequency = 1000L;

    private static final long RejectSnowFlakeStrategyForSingleModeCacheFrequency = 1000L;

    private static final long SelfCorrectSnowFlakeStrategyForSingleModeCacheFrequency = 1000L;

    private static final long SpinSnowFlakeStrategyForSingleModeCacheFrequency = 1000L;

    public static Map<String, Long> cacheFrequencyMap = new HashMap();

    static {
        cacheFrequencyMap.put(IdGenerateType.RejectSnowFlakeStrategyForClusterModeType, RejectSnowFlakeStrategyForClusterModeCacheFrequency);
        cacheFrequencyMap.put(IdGenerateType.SelfCorrectSnowFlakeStrategyForClusterModeType, SelfCorrectSnowFlakeStrategyForClusterModeCacheFrequency);
        cacheFrequencyMap.put(IdGenerateType.SpinSnowFlakeStrategyForClusterModeType, SpinSnowFlakeStrategyForClusterModeCacheFrequency);
        cacheFrequencyMap.put(IdGenerateType.RejectSnowFlakeStrategyForSingleModeType, RejectSnowFlakeStrategyForSingleModeCacheFrequency);
        cacheFrequencyMap.put(IdGenerateType.SelfCorrectSnowFlakeStrategyForSingleModeType, SelfCorrectSnowFlakeStrategyForSingleModeCacheFrequency);
        cacheFrequencyMap.put(IdGenerateType.SpinSnowFlakeStrategyForSingleModeType, SpinSnowFlakeStrategyForSingleModeCacheFrequency);
    }

}
