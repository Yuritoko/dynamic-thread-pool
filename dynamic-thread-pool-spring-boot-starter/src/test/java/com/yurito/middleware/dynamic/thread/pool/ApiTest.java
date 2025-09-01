package com.yurito.middleware.dynamic.thread.pool;

import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.junit.Test;
import org.redisson.api.RTopic;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author Yurito
 * @description: 单元测试
 * @create 2025/9/1 14:50
 */
public class ApiTest {

    @Resource
    private RTopic dynamicThreadPoolRedisTopic;

    @Test
    public void test_dynamicThreadPoolRedisTopic() throws InterruptedException {
        ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity("dynamic-thread-pool-test-app", "threadPoolExecutor01");
        threadPoolConfigEntity.setPoolSize(100);
        threadPoolConfigEntity.setMaximumPoolSize(100);
        dynamicThreadPoolRedisTopic.publish(threadPoolConfigEntity);

        new CountDownLatch(1).await();
    }

}
