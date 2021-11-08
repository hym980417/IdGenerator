package com.example.demo.service.impl;


import com.example.demo.strategy.IdGenerateStrategy;
import com.example.demo.service.IdGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class IdGenerateServiceImpl implements IdGenerateService {

    @Autowired
    @Qualifier("RejectSnowFlakeStrategyForSingleMode")
    private IdGenerateStrategy idGenerateStrategyForSingleMode;

    @Autowired
    @Qualifier("RejectSnowFlakeStrategyForClusterMode")
    private IdGenerateStrategy idGenerateStrategyForClusterMode;

    @Override
    public Long getGeneratedIdFromSingle(String increaseTYpe) {

        return idGenerateStrategyForSingleMode.getNextId(increaseTYpe);

    }

    @Override
    public Long getGeneratedIdFromCluster(String increaseTYpe) {

        return idGenerateStrategyForClusterMode.getNextId(increaseTYpe);

    }
}
