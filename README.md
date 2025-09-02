# Dynamic Thread Pool 动态线程池组件

## 项目简介

Dynamic Thread Pool 是一个基于 Java 的动态线程池管理组件，旨在解决传统线程池配置固定、难以动态调整的问题。该组件提供了运行时动态调整线程池参数的能力，包括核心线程数、最大线程数、队列容量等关键配置，并支持通过 Redis 实现分布式环境下的线程池配置同步。

该组件的设计灵感来源于美团技术团队关于线程池在业务中实践的分享，致力于提供一种简单易用、可视化、可监控的线程池动态管理方案。

## 开发背景

在传统的 Java 应用中，线程池配置通常在应用启动时就固定下来，难以根据运行时的负载情况进行动态调整。这可能导致以下问题：

1. **资源浪费**：在低负载时，线程池中的线程资源可能被过度分配
2. **性能瓶颈**：在高负载时，固定的线程池配置可能无法满足业务需求
3. **运维困难**：调整线程池配置需要重启应用，影响业务连续性
4. **缺乏监控**：传统线程池对任务执行情况透明度低，难以区分不同业务任务的执行情况

为了解决这些问题，我们开发了 Dynamic Thread Pool 组件，允许在运行时动态调整线程池配置，提升系统的自适应能力，并提供丰富的监控功能。

## 核心原理

本组件基于 Java 的 ThreadPoolExecutor 实现，参考了美团技术团队在线程池实践中的经验总结，主要包含以下核心设计：

### 1. 线程池参数动态调整
- 基于 ThreadPoolExecutor 提供的 setCorePoolSize、setMaximumPoolSize 等方法实现参数动态调整
- 自定义可调整容量的阻塞队列 ResizableCapacityLinkedBlockingQueue，支持运行时调整队列容量
- 通过 Redis 实现分布式环境下的配置同步

### 2. 任务级精细化监控
- 提供任务级别的执行监控，可区分不同业务任务的执行情况
- 支持为不同业务任务指定具有业务含义的名称
- 实现拒绝任务计数功能，通过装饰器模式包装拒绝策略，统计被拒绝的任务数量

### 3. 运行时状态实时查看
- 封装 ThreadPoolExecutor 的 getter 方法，提供线程池运行时状态的实时查看功能
- 实时监控活跃线程数、队列任务数、已完成任务数等关键指标

## 使用条件

### 环境要求

- JDK 1.8 或更高版本
- Maven 3.0+
- Redis 服务（用于分布式配置同步）
- Spring Boot 2.7.x（可选，用于集成 starter）

### 依赖组件

- Redisson 3.26.0（用于 Redis 连接和分布式同步）
- FastJSON 2.0.49（用于序列化）
- Spring Boot 相关组件（如使用 starter）

## 核心功能

### 1. 动态线程池参数调整

- 核心线程数动态调整
- 最大线程数动态调整
- 队列容量动态调整（支持可调整容量的队列）
- 拒绝策略查看

### 2. 实时监控

- 线程池状态实时监控
- 活跃线程数监控
- 队列任务数监控
- 已完成任务数统计
- 拒绝任务数统计
- 任务级别执行情况监控

### 3. 分布式支持

- 基于 Redis 的配置同步机制
- 多实例配置一致性保证
- 配置变更实时通知

### 4. 可视化管理界面

- Web 管理界面
- 线程池列表展示
- 参数修改界面
- 实时数据刷新

## 技术实现详解

### 1. 核心技术架构

#### 1.1 Spring Boot自动配置机制
项目采用了Spring Boot的自动配置机制，通过[DynamicThreadPoolAutoConfig](file:///C:/Users/admin/Desktop/dynamic-thread-pool/dynamic-thread-pool-spring-boot-starter/src/main/java/com/yurito/middleware/dynamic/thread/pool/sdk/config/DynamicThreadPoolAutoConfig.java#L37-L114)类实现自动装配：

- 使用[@Configuration](file:///C:/Users/admin/Desktop/dynamic-thread-pool/dynamic-thread-pool-spring-boot-starter/src/main/java/com/yurito/middleware/dynamic/thread/pool/sdk/config/DynamicThreadPoolAutoConfig.java#L37-L114)注解标识这是一个配置类
- 使用[@EnableConfigurationProperties](file:///C:/Users/admin/Desktop/dynamic-thread-pool/dynamic-thread-pool-spring-boot-starter/src/main/java/com/yurito/middleware/dynamic/thread/pool/sdk/config/DynamicThreadPoolAutoConfig.java#L37-L114)启用配置属性绑定
- 使用[@EnableScheduling](file:///C:/Users/admin/Desktop/dynamic-thread-pool/dynamic-thread-pool-spring-boot-starter/src/main/java/com/yurito/middleware/dynamic/thread/pool/sdk/config/DynamicThreadPoolAutoConfig.java#L37-L114)启用定时任务功能

这种方式让用户只需引入starter依赖，即可自动完成线程池监控和动态配置功能的装配。

#### 1.2 Redisson实现分布式协调
项目使用Redisson作为Redis客户端，实现分布式环境下的配置同步：

- 通过RedisRegistry类实现线程池配置的注册和上报
- 使用RTopic实现配置变更的发布/订阅机制
- 使用RBucket和RList存储线程池配置数据

RedisRegistry中的关键方法：
```java
// 上报线程池列表
@Override
public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities) {
    RList<ThreadPoolConfigEntity> list = redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey());
    list.clear();
    list.addAll(threadPoolEntities);
}

// 上报线程池配置参数
@Override
public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
    String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
    RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(cacheKey);
    bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
}
```

#### 1.3 观察者模式实现配置变更监听
通过ThreadPoolConfigAdjustListener实现配置变更的监听和处理：

- 实现Redisson的MessageListener接口
- 监听配置变更消息并更新线程池参数
- 更新后自动上报最新配置到Redis

```java
@Override
public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
    logger.info("动态线程池，调整线程池配置。线程池名称:{} 核心线程数:{} 最大线程数:{}", 
        threadPoolConfigEntity.getThreadPoolName(), 
        threadPoolConfigEntity.getPoolSize(), 
        threadPoolConfigEntity.getMaximumPoolSize());
    
    // 更新线程池配置
    dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);
    
    // 上报最新数据
    List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
    registry.reportThreadPool(threadPoolConfigEntities);
}
```

### 2. 核心功能实现

#### 2.1 可动态调整容量的阻塞队列
ResizableCapacityLinkedBlockingQueue是项目的核心创新之一，支持运行时动态调整队列容量：

```java
public void setCapacity(int newCapacity) throws InterruptedException {
    if (newCapacity <= 0) {
        throw new IllegalArgumentException("Capacity must be positive");
    }
    
    lock.lockInterruptibly();
    try {
        // 1. 创建新队列
        LinkedBlockingQueue<E> newQueue = new LinkedBlockingQueue<>(newCapacity);
        
        // 2. 迁移数据
        synchronized (this) {
            E element;
            while ((element = queue.poll()) != null) {
                // 如果新队列满了，说明是缩容且旧队列数据太多
                if (!newQueue.offer(element)) {
                    // 将元素重新放回原队列
                    queue.offer(element);
                    throw new IllegalStateException("New capacity is too small to hold current elements");
                }
            }
            
            // 3. 原子性地切换引用
            queue = newQueue;
            this.capacity = newCapacity;
        }
    } finally {
        lock.unlock();
    }
}
```

#### 2.2 拒绝策略装饰器
RejectedExecutionHandlerDecorator通过装饰器模式为线程池添加拒绝任务计数功能：

```java
@Override
public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    // 增加拒绝任务计数
    AtomicLong counter = REJECTED_COUNT_MAP.get(threadPoolName);
    if (counter != null) {
        long count = counter.incrementAndGet();
        logger.info("线程池 {} 拒绝任务数增加至: {}", threadPoolName, count);
    }
    
    // 执行原始拒绝策略
    delegate.rejectedExecution(r, executor);
}
```

#### 2.3 定时任务数据上报
ThreadPoolDataReportJob通过Spring的[@Scheduled](file:///C:/Users/admin/Desktop/dynamic-thread-pool/dynamic-thread-pool-spring-boot-starter/src/main/java/com/yurito/middleware/dynamic/thread/pool/sdk/trigger/job/ThreadPoolDataReportJob.java#L37-L43)注解实现定时上报线程池状态：

```java
@Scheduled(cron = "0/20 * * * * ?")
public void execReportThreadPoolList(){
    List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
    registry.reportThreadPool(threadPoolConfigEntities);
    logger.info("动态线程池，上报线程池信息：{}", JSON.toJSONString(threadPoolConfigEntities));

    for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigEntities) {
        registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
        logger.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
    }
}
```

### 3. 前端技术

#### 3.1 原生HTML/CSS/JavaScript实现管理界面
前端采用原生技术实现，无框架依赖，包括：

- 使用CSS3实现现代化UI界面和动画效果
- 使用原生JavaScript实现数据获取和交互
- 通过XMLHttpRequest与后端API通信

关键JavaScript代码：
```javascript
function fetchThreadPoolList() {
    loader.style.display = 'block';
    var xhr = new XMLHttpRequest();
    xhr.open('GET', 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list', true);
    xhr.onload = function() {
        loader.style.display = 'none';
        if (xhr.status >= 200 && xhr.status < 300) {
            var response = JSON.parse(xhr.responseText);
            if (response.code === "0000" && Array.isArray(response.data)) {
                updateThreadPoolList(response.data);
            } else {
                console.error('The request was successful but the data format is incorrect!');
            }
        } else {
            console.error('The request failed!');
        }
    };
    xhr.onerror = function() {
        loader.style.display = 'none';
        console.error('The request failed!');
    };
    xhr.send();
}
```

## 项目模块说明

### dynamic-thread-pool-spring-boot-starter

核心功能模块，提供线程池动态调整和监控能力：
- 线程池配置动态调整实现
- Redis 配置同步机制
- 监控数据收集和上报
- 配置变更监听器

### dynamic-thread-pool-admin

管理端模块，提供 Web 管理界面：
- RESTful API 接口
- 前端管理页面
- 配置修改和查询接口

### dynamic-thread-pool-test

测试模块，演示组件使用方式：
- 线程池配置示例
- 测试任务生成
- 集成示例

## 开发中的问题

### 1. 队列容量调整的可见性问题

在实际使用中，虽然队列容量调整功能已经实现，但在某些场景下用户可能难以直观感受到容量变化：
- 当线程池任务处理速度较快时，队列很少达到满状态
- 需要足够大的任务负载才能填满队列以验证容量调整效果

### 2. 线程池参数一致性问题

在分布式环境下，线程池配置变更可能存在短暂的不一致：
- 配置变更通知存在网络延迟
- 不同节点应用配置的时间点可能不一致

### 3. 异常处理机制

在队列容量调整过程中，如果新容量小于当前队列中的任务数，可能导致异常：
- 需要合理处理容量缩小时的任务溢出问题
- 提供更完善的异常提示和恢复机制

## 待改进方向

### 1. 增强监控能力

- 添加线程池历史数据统计功能
- 提供线程池性能趋势分析
- 增加告警机制（如队列积压告警）

### 2. 优化用户界面

- 提供更直观的队列容量调整效果展示
- 增加线程池状态可视化图表
- 支持批量配置修改功能

### 3. 完善配置管理

- 支持配置版本管理和回滚
- 提供配置导入导出功能
- 增加配置变更审批流程

### 4. 提升系统稳定性

- 增强异常情况下的容错能力
- 提供配置变更失败的自动恢复机制
- 优化分布式环境下的配置同步性能

### 5. 扩展功能支持

- 支持更多类型的阻塞队列
- 提供线程池健康检查功能
- 增加线程池配置模板管理

## 使用示例

### 集成 starter

在 Spring Boot 项目中添加依赖：

```xml
<dependency>
    <groupId>com.yurito.middleware</groupId>
    <artifactId>dynamic-thread-pool-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

### 配置文件设置

```yaml
dynamic:
  thread:
    pool:
      config:
        enabled: true
        # Redis配置
        host: 127.0.0.1
        port: 6379
```

### 线程池配置

```java
@Bean("threadPoolExecutor01")
public ThreadPoolExecutor threadPoolExecutor01(ThreadPoolConfigProperties properties) {
    // 使用可调整容量的队列
    ResizableCapacityLinkedBlockingQueue<Runnable> queue = 
        new ResizableCapacityLinkedBlockingQueue<>(properties.getBlockQueueSize());
    
    return new ThreadPoolExecutor(
            properties.getCorePoolSize(),
            properties.getMaxPoolSize(),
            properties.getKeepAliveTime(),
            TimeUnit.SECONDS,
            queue);
}
```

## 项目结构

```
dynamic-thread-pool/
├── dynamic-thread-pool-spring-boot-starter/  # 核心功能模块
├── dynamic-thread-pool-admin/               # 管理端模块
├── dynamic-thread-pool-test/                # 测试模块
└── docs/                                    # 文档和前端页面
```

## 实践总结

面对业务中使用线程池遇到的实际问题，我们参考了美团技术团队的实践经验，从以下三个方向进行了探索：

1. **替代方案探索**：考虑过使用 Actor 模型、协程等替代方案，但这些方案在 Java 生态中还不够成熟，且学习成本较高

2. **参数设置合理性追求**：尝试寻找通用的线程池参数计算公式，但发现并发任务的执行情况和任务类型相关性较大，IO 密集型和 CPU 密集型任务差异很大，难以有统一的计算方式

3. **参数动态化**：最终选择将线程池参数从代码中迁移到分布式配置中心上，实现线程池参数可动态配置和即时生效

通过参数动态化方案，我们在成本和收益之间取得了良好平衡：
- 成本：实现动态化以及监控成本不高
- 收益：在不颠覆原有线程池使用方式的基础上，从降低线程池参数修改的成本以及多维度监控这两个方面降低了故障发生的概率