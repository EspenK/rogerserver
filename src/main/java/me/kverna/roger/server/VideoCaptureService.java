package me.kverna.roger.server;

import lombok.Getter;
import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Log
public class VideoCaptureService implements Runnable {

    @Getter private int bufferSize;

    private Camera camera;
    private InputStream stream;

    private List<VideoFeedListener> videoFeedListeners;

    private boolean processing = true;

    public VideoCaptureService(Camera camera) throws IOException {
        this(camera, 512);
    }

    public VideoCaptureService(Camera camera, int bufferSize) throws IOException {
        this.camera = camera;
        this.bufferSize = bufferSize;

        videoFeedListeners = new ArrayList<>();

        // Parse the camera URL and open a connection
        URL url = new URL(camera.getUrl());
        log.info(String.format("Opening connection for %s: %s", camera.getName(), url));
        URLConnection connection = url.openConnection();

        // Set the camera stream
        this.stream = connection.getInputStream();
    }

    public void addListener(VideoFeedListener listener) {
        videoFeedListeners.add(listener);
    }

    public void removeListener(VideoFeedListener listener) {
        videoFeedListeners.remove(listener);
    }

    public synchronized void stop() {
        processing = false;
    }

    @Override
    public void run() {
        byte[] buffer;
        
        while (processing) {
            // Read a chunk and send to all listeners
            try {
                buffer = stream.readNBytes(bufferSize);
                for (VideoFeedListener listener : videoFeedListeners) {
                    // Remove closed listeners
                    if (listener.isAlive()) {
                        listener.process(buffer);
                    } else {
                        videoFeedListeners.remove(listener);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
