package com.yurito;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Yurito
 * @description
 * @create 2025/9/1 15:49
 */
@SpringBootApplication
@Configuration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public ApplicationRunner applicationRunner(ExecutorService threadPoolExecutor01) {
        return args -> {
            // 创建后台守护线程来运行任务生成器
            Thread taskGeneratorThread = new Thread(() -> {
                int taskCount = 0;
                try {
                    // 先提交足够多的任务来填满队列
                    for (int i = 0; i < 10; i++) {
                        final int taskId = taskCount++;
                        threadPoolExecutor01.submit(() -> {
                            try {
                                // 模拟中等时间运行的任务，以便更好地测试队列
                                System.out.println("Task " + taskId + " started.");
                                TimeUnit.SECONDS.sleep(60); // 60秒的任务
                                System.out.println("Task " + taskId + " completed.");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                System.out.println("Task " + taskId + " interrupted.");
                            }
                        });
                    }
                    
                    System.out.println("Initial tasks submitted.");
                    
                    // 等待一段时间后再提交更多任务
                    TimeUnit.SECONDS.sleep(30);
                    
                    // 再提交一批任务来测试队列容量
                    for (int i = 0; i < 10; i++) {
                        final int taskId = taskCount++;
                        threadPoolExecutor01.submit(() -> {
                            try {
                                // 模拟中等时间运行的任务，以便更好地测试队列
                                System.out.println("Task " + taskId + " started.");
                                TimeUnit.SECONDS.sleep(60); // 60秒的任务
                                System.out.println("Task " + taskId + " completed.");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                System.out.println("Task " + taskId + " interrupted.");
                            }
                        });
                    }
                    
                    System.out.println("Additional tasks submitted.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Task generator interrupted.");
                }
            });

            // 设置为守护线程，确保不会阻止应用关闭
            taskGeneratorThread.setDaemon(true);
            taskGeneratorThread.start();
            
            System.out.println("Task generator started. Submitting tasks to test queue capacity.");
        };
    }

}