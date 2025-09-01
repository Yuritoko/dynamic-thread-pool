package com.yurito.middleware.dynamic.thread.pool.sdk.registry;

import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @author Yurito
 * @description 注册中心接口，可使用多种策略，如Redis、zookeeper等
 * @create 2025/9/1 17:01
 */
public interface IRegistry {

    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities);

    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);
}
