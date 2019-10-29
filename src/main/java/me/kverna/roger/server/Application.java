package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Log
@SpringBootApplication
public class Application {

    private final CameraService cameraService;

    public Application(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Application app = context.getBean(Application.class);
        app.run();
    }

    /**
     * Start a service for all cameras upon startup.
     */
    private void run() {
        for (Camera camera : cameraService.findAllCameras()) {
            cameraService.startCaptureService(camera);
        }
    }
}

