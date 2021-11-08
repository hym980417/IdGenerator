package com.example.demo.strategy;

/**
 * @author ming
 * @date 2021/11/6 18:58
 */
public interface IdGenerateStrategy {

    /**
     * 获取下一个id
     *
     * @param increaseType
     * @return
     */
    long getNextId(String increaseType);


}
