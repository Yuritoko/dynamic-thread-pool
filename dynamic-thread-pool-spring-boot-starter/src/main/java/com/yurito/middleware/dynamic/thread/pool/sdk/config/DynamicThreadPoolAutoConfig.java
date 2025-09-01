package com.yurito.middleware.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson2.JSON;
import com.yurito.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Yurito
 * @description 动态配置中心入口
 * @create 2025/9/1 14:49
 */
@Configuration
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    @Bean("dynamicThreadPollService")
    public DynamicThreadPoolService dynamicThreadPollService(ApplicationContext applicationContext, Map<String,ThreadPoolExecutor> threadPoolExecutorMap){
        // String applicationName = applicationContext.getApplicationName();
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "缺省的";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }

        return new DynamicThreadPoolService(applicationName,threadPoolExecutorMap);
    }
}
