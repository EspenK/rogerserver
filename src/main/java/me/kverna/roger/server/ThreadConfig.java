package me.kverna.roger.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class ThreadConfig {
    @Bean(name = "serviceExecutor")
    public TaskExecutor serviceExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
