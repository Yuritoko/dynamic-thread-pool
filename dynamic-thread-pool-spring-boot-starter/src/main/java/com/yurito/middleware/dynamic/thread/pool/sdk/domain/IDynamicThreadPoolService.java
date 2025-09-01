package com.yurito.middleware.dynamic.thread.pool.sdk.domain;

import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @author Yurito
 * @description 动态线程池服务
 * @create 2025/9/1 16:20
 */
public interface IDynamicThreadPoolService {

    List<ThreadPoolConfigEntity> queryTreadPoolList();

    ThreadPoolConfigEntity queryTreadPoolConfigByName(String threadPoolName);

    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

}
