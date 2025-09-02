# Dynamic Thread Pool 动态线程池组件

## 项目简介

Dynamic Thread Pool 是一个基于 Java 的动态线程池管理组件，旨在解决传统线程池配置固定、难以动态调整的问题。该组件提供了运行时动态调整线程池参数的能力，包括核心线程数、最大线程数、队列容量等关键配置，并支持通过 Redis 实现分布式环境下的线程池配置同步。

## 开发背景

在传统的 Java 应用中，线程池配置通常在应用启动时就固定下来，难以根据运行时的负载情况进行动态调整。这可能导致以下问题：

1. **资源浪费**：在低负载时，线程池中的线程资源可能被过度分配
2. **性能瓶颈**：在高负载时，固定的线程池配置可能无法满足业务需求
3. **运维困难**：调整线程池配置需要重启应用，影响业务连续性

为了解决这些问题，我们开发了 Dynamic Thread Pool 组件，允许在运行时动态调整线程池配置，提升系统的自适应能力。

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

### 3. 分布式支持

- 基于 Redis 的配置同步机制
- 多实例配置一致性保证
- 配置变更实时通知

### 4. 可视化管理界面

- Web 管理界面
- 线程池列表展示
- 参数修改界面
- 实时数据刷新

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