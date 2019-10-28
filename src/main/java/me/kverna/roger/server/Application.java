package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Log
@SpringBootApplication
public class Application {

    private final CameraService cameraService;

    public Application(CameraService cameraService) {
        this.cameraService = cameraService;
    }

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
            for (Camera camera : cameraService.findAllCameras()) {
                VideoCaptureService captureService = new VideoCaptureService(camera);
                cameraService.setCaptureService(camera, captureService);
                executor.execute(captureService);
            }
        };
    }
}

