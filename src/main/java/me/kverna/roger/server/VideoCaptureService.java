package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

@Log
public class VideoCaptureService implements Runnable {

    private Camera camera;

    private boolean processing = true;

    public VideoCaptureService(Camera camera) throws IOException {
        this.camera = camera;

        // Parse the camera URL and open a connection
        URL url = new URL(camera.getUrl());
        log.info(String.format("Opening connection for %s: %s", camera.getName(), url));
        URLConnection connection = url.openConnection();

        // Route the input stream to the camera object
        camera.setStream(connection.getInputStream());
    }

    public synchronized void stop() {
        processing = false;
    }

    @Override
    public void run() {
        /*while (processing) {

        }*/
    }
}
