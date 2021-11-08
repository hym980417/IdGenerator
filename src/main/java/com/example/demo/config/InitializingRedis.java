package com.example.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author ming
 * @date 2021/11/6 13:40
 */
@Configuration
public class InitializingRedis {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedissonClient getRedissonClient() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+host+":"+port);
        config.useSingleServer().setPassword(password);
        config.useSingleServer().setConnectTimeout(30000);
        config.useSingleServer().setConnectionPoolSize(8);
        config.useSingleServer().setConnectionMinimumIdleSize(8);
        return Redisson.create(config);
    }
}
