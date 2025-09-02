package com.yurito.config;

import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.RejectedExecutionHandlerDecorator;
import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.ResizableCapacityLinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {

    @Bean("threadPoolExecutor01")
    public ThreadPoolExecutor threadPoolExecutor01(ThreadPoolConfigProperties properties) {
        // 实例化策略
        RejectedExecutionHandler handler;
        switch (properties.getPolicy().toLowerCase()) {
            case "abortpolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "discardpolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "discardoldestpolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "callerrunspolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        
        // 创建可调整容量的队列
        ResizableCapacityLinkedBlockingQueue<Runnable> queue = 
            new ResizableCapacityLinkedBlockingQueue<>(properties.getBlockQueueSize());
        
        // 使用装饰器包装拒绝策略，以支持拒绝任务计数
        RejectedExecutionHandler decoratedHandler = new RejectedExecutionHandlerDecorator(handler, "threadPoolExecutor01");

        // 创建线程池
        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                queue,
                Executors.defaultThreadFactory(),
                decoratedHandler);
    }

    @Bean("threadPoolExecutor02")
    public ThreadPoolExecutor threadPoolExecutor02(ThreadPoolConfigProperties properties) {
        // 实例化策略
        RejectedExecutionHandler handler;
        switch (properties.getPolicy().toLowerCase()) {
            case "abortpolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "discardpolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "discardoldestpolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "callerrunspolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        
        // 创建可调整容量的队列
        ResizableCapacityLinkedBlockingQueue<Runnable> queue = 
            new ResizableCapacityLinkedBlockingQueue<>(properties.getBlockQueueSize());
        
        // 使用装饰器包装拒绝策略，以支持拒绝任务计数
        RejectedExecutionHandler decoratedHandler = new RejectedExecutionHandlerDecorator(handler, "threadPoolExecutor02");

        // 创建线程池
        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                queue,
                Executors.defaultThreadFactory(),
                decoratedHandler);
    }

}