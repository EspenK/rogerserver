package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

@Log
public class VideoCaptureService implements Runnable {

    private Camera camera;

    private boolean processing = true;

    public VideoCaptureService(CameraService cameraService, Camera camera) throws IOException {
        this.camera = camera;

        // Parse the camera URL and open a connection
        URL url = new URL(camera.getUrl());
        log.info(String.format("Opening connection for %s: %s", camera.getName(), url));
        URLConnection connection = url.openConnection();

        // Set the camera stream
        cameraService.setCameraStream(camera, connection.getInputStream());
    }

    public synchronized void stop() {
        processing = false;
    }

    @Override
    public void run() {
        while (processing) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                stop();
            }
        }
    }
}
