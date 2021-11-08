package com.example.demo;

import com.example.demo.service.IdGenerateService;
import com.example.demo.strategy.impl.single.RejectSnowFlakeStrategyForSingleMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private IdGenerateService idGenerateService;

	@Test
	void contextLoads() {
		long time1 = System.currentTimeMillis();
		final Long[] sum = {0L};
		for (int i = 0;i < 1;i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					for(int i = 0;i<=10;i++){

						System.out.println(idGenerateService.getGeneratedIdFromSingle(RejectSnowFlakeStrategyForSingleMode.REGULAR_INCREASE_TYPE));
					}
					sum[0] +=1000;
					long time3 = System.currentTimeMillis();
					long qpms = sum[0] /(time3-time1);
					System.out.println("done:"+qpms);
				}
			}).start();

		}
		long time2 = System.currentTimeMillis();
		long qpms = sum[0] /(time2-time1);
		System.out.println("done:"+qpms);
	}

}
