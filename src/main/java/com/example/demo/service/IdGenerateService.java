package com.example.demo.service;

/**
 * @author ming
 * @date 2021/11/5 21:51
 */
public interface IdGenerateService {
    /**
     * 根据增长类型从集群获取id
     * @param increaseType
     * @return
     */
    Long getGeneratedIdFromSingle(String increaseType);

    /**
     * 根据增长类型从单机获取id
     * @param increaseType
     * @return
     */
    Long getGeneratedIdFromCluster(String increaseType);
}
