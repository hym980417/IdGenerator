package com.example.demo.controller;

import com.example.demo.service.IdGenerateService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ming
 * @date 2021/11/5 22:23
 */
@Controller
public class IdGenerateController {
    @Autowired
    private IdGenerateService idGenerateService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private double sum = 0;

    private double startTime = 0;

    /**
     * 步长增长类型
     */
    public final static String REGULAR_INCREASE_TYPE = "REGULAR";//步长有规律的增长
    public final static String IRREGULAR_INCREASE_TYPE = "IRREGULAR";//步长无规律的增长

    private Map<String, Integer> map = new HashMap<>();

    /**
     * 测试
     */
    @GetMapping("/helloTest")
    @ResponseBody
    public String helloTest() {
        return "hello!";
    }

    /**
     * 集群接口
     *
     * @param increaseType
     * @return
     */
    @GetMapping("/getIdFromClusterNodes")
    @ResponseBody
    public long getIdFromClusterNodes(@RequestParam(name = "increaseType") String increaseType) {
        long id = idGenerateService.getGeneratedIdFromCluster(increaseType);
        return id;
    }


    /**
     * 单机接口
     *
     * @param increaseType
     * @return
     */
    @GetMapping("/getIdFromSingleNodes")
    @ResponseBody
    public long getIdFromSingleNodes(@RequestParam(name = "increaseType") String increaseType) {
        long id = idGenerateService.getGeneratedIdFromSingle(increaseType);
        return id;
    }

//    /**
//     * 集群测试接口
//     *
//     * @param increaseType
//     * @return
//     */
//    @RequestMapping("/getIdFromClusterNodesBatchTest")
//    @ResponseBody
//    public void getIdFromClusterNodesBatchTest(@RequestParam(name = "increaseType") String increaseType) {
//
//        if (startTime == 0L) {
//            startTime = System.currentTimeMillis();
//        }
//
//        for (int i = 0; i < 1000; i++) {
//            for (int j = 0; j <= 1000; j++) {
//                idGenerateService.getGeneratedIdFromCluster(increaseType);
//            }
//            long time3 = System.currentTimeMillis();
//            sum += 1000;
//            double qps = sum / (time3 - startTime) * 1000;
//            System.out.println("当前总量：" + sum + " 当前时间：" + (time3 - startTime) * 1000 + " 每秒产生量:" + qps);
//        }
//    }
//
//
//    /**
//     * 单机测试接口
//     *
//     * @param increaseType
//     * @return
//     */
//    @RequestMapping("/getIdFromSingleNodeBatchTest")
//    @ResponseBody
//    public void getIdFromSingleNodeBatchTest(@RequestParam(name = "increaseType") String increaseType) {
//
//        if (startTime == 0L) {
//            startTime = System.currentTimeMillis();
//        }
//
//        for (int i = 0; i < 1000; i++) {
//            for (int j = 0; j <= 1000; j++) {
//                idGenerateService.getGeneratedIdFromSingle(increaseType);
//            }
//            long time3 = System.currentTimeMillis();
//            sum += 1000;
//            double qps = sum / (time3 - startTime) * 1000;
//            System.out.println("当前总量：" + sum + " 当前时间：" + (time3 - startTime) * 1000 + " 每秒产生量:" + qps);
//        }
//    }
}
