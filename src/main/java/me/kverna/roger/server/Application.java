package me.kverna.roger.server;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
@Log
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "mainExecutor")
    public TaskExecutor mainExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner startServices(@Qualifier("mainExecutor") TaskExecutor executor) {
        return args -> {
            // TODO: Add services to start
            // executor.execute(new Service());
        };
    }
}
