package com.yurito.middleware.dynamic.thread.pool.sdk.domain;

import com.alibaba.fastjson.JSON;
import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.RejectedExecutionHandlerDecorator;
import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.ResizableCapacityLinkedBlockingQueue;
import com.yurito.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Yurito
 * @description 动态线程池服务
 * @create 2025/9/1 16:26
 */
public class DynamicThreadPoolService implements IDynamicThreadPoolService{

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolService.class);

    private final String applicationName;

    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolService(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        Set<String> threadPoolBeanNames = threadPoolExecutorMap.keySet();
        List<ThreadPoolConfigEntity> threadPoolVOS = new ArrayList<>(threadPoolBeanNames.size());
        for (String beanName : threadPoolBeanNames) {
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(beanName);
            ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(applicationName, beanName);
            threadPoolConfigVO.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
            threadPoolConfigVO.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
            threadPoolConfigVO.setActiveCount(threadPoolExecutor.getActiveCount());
            threadPoolConfigVO.setPoolSize(threadPoolExecutor.getPoolSize());
            threadPoolConfigVO.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
            threadPoolConfigVO.setQueueSize(threadPoolExecutor.getQueue().size());
            threadPoolConfigVO.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());
            
            // 设置队列容量（如果队列支持动态调整）
            if (threadPoolExecutor.getQueue() instanceof ResizableCapacityLinkedBlockingQueue) {
                ResizableCapacityLinkedBlockingQueue<?> resizableQueue = 
                    (ResizableCapacityLinkedBlockingQueue<?>) threadPoolExecutor.getQueue();
                threadPoolConfigVO.setQueueCapacity(resizableQueue.getCapacity());
            }
            
            // 获取拒绝策略名称
            threadPoolConfigVO.setRejectPolicy(getRejectPolicyName(threadPoolExecutor));
            threadPoolConfigVO.setCompletedTaskCount(threadPoolExecutor.getCompletedTaskCount());
            // 获取拒绝任务数
            long rejectedTaskCount = getRejectedTaskCount(beanName, threadPoolExecutor);
            threadPoolConfigVO.setRejectedTaskCount(rejectedTaskCount);
            logger.debug("线程池 {} 当前拒绝任务数: {}", beanName, rejectedTaskCount);
            threadPoolVOS.add(threadPoolConfigVO);
        }
        return threadPoolVOS;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolName);
        if (null == threadPoolExecutor) return new ThreadPoolConfigEntity(applicationName, threadPoolName);

        // 线程池配置数据
        ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(applicationName, threadPoolName);
        threadPoolConfigVO.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
        threadPoolConfigVO.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        threadPoolConfigVO.setActiveCount(threadPoolExecutor.getActiveCount());
        threadPoolConfigVO.setPoolSize(threadPoolExecutor.getPoolSize());
        threadPoolConfigVO.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
        threadPoolConfigVO.setQueueSize(threadPoolExecutor.getQueue().size());
        threadPoolConfigVO.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());
        
        // 设置队列容量（如果队列支持动态调整）
        if (threadPoolExecutor.getQueue() instanceof ResizableCapacityLinkedBlockingQueue) {
            ResizableCapacityLinkedBlockingQueue<?> resizableQueue = 
                (ResizableCapacityLinkedBlockingQueue<?>) threadPoolExecutor.getQueue();
            threadPoolConfigVO.setQueueCapacity(resizableQueue.getCapacity());
        }
        
        // 获取拒绝策略名称
        threadPoolConfigVO.setRejectPolicy(getRejectPolicyName(threadPoolExecutor));
        threadPoolConfigVO.setCompletedTaskCount(threadPoolExecutor.getCompletedTaskCount());
        // 获取拒绝任务数
        long rejectedTaskCount = getRejectedTaskCount(threadPoolName, threadPoolExecutor);
        threadPoolConfigVO.setRejectedTaskCount(rejectedTaskCount);
        logger.debug("线程池 {} 当前拒绝任务数: {}", threadPoolName, rejectedTaskCount);

        if (logger.isDebugEnabled()) {
            logger.info("动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, threadPoolName, JSON.toJSONString(threadPoolConfigVO));
        }

        return threadPoolConfigVO;
    }
    
    /**
     * 获取拒绝策略名称
     * 
     * @param threadPoolExecutor 线程池执行器
     * @return 拒绝策略名称
     */
    private String getRejectPolicyName(ThreadPoolExecutor threadPoolExecutor) {
        // 如果使用了我们的装饰器，获取原始拒绝策略名称
        if (threadPoolExecutor.getRejectedExecutionHandler() instanceof RejectedExecutionHandlerDecorator) {
            return ((RejectedExecutionHandlerDecorator) threadPoolExecutor.getRejectedExecutionHandler()).getName();
        }
        // 否则返回直接拒绝策略类名
        return threadPoolExecutor.getRejectedExecutionHandler().getClass().getSimpleName();
    }
    
    /**
     * 获取线程池的拒绝任务数
     * 
     * @param threadPoolName 线程池名称
     * @param threadPoolExecutor 线程池执行器
     * @return 拒绝任务数
     */
    private long getRejectedTaskCount(String threadPoolName, ThreadPoolExecutor threadPoolExecutor) {
        // 如果使用了我们的装饰器，获取装饰器中的计数
        if (threadPoolExecutor.getRejectedExecutionHandler() instanceof RejectedExecutionHandlerDecorator) {
            return RejectedExecutionHandlerDecorator.getRejectedCount(threadPoolName);
        }
        // 如果没有使用装饰器，返回默认值0
        logger.debug("线程池 {} 未使用拒绝任务计数装饰器，返回默认值0", threadPoolName);
        return 0;
    }

    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if (null == threadPoolConfigEntity || !applicationName.equals(threadPoolConfigEntity.getAppName())) return;
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if (null == threadPoolExecutor) return;

        try {
            // 设置参数
            if (threadPoolConfigEntity.getCorePoolSize() > 0) {
                threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            }
            
            if (threadPoolConfigEntity.getMaximumPoolSize() > 0) {
                threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
            }
            
            // 如果队列支持动态调整容量，尝试调整队列容量
            BlockingQueue<Runnable> queue = threadPoolExecutor.getQueue();
            if (queue instanceof ResizableCapacityLinkedBlockingQueue && 
                threadPoolConfigEntity.getQueueCapacity() > 0) {
                ResizableCapacityLinkedBlockingQueue<Runnable> resizableQueue = 
                    (ResizableCapacityLinkedBlockingQueue<Runnable>) queue;
                try {
                    resizableQueue.setCapacity(threadPoolConfigEntity.getQueueCapacity());
                    logger.info("动态线程池 {} 队列容量已调整为: {}", 
                        threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity.getQueueCapacity());
                } catch (Exception e) {
                    logger.error("动态线程池 {} 队列容量调整失败，新容量: {}", 
                        threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity.getQueueCapacity(), e);
                }
            }
        } catch (Exception e) {
            logger.error("动态线程池 {} 参数调整失败", threadPoolConfigEntity.getThreadPoolName(), e);
        }
    }
}