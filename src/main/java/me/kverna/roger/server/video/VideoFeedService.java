package me.kverna.roger.server.video;

import lombok.Getter;
import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Log
public class VideoFeedService implements Runnable {

    private Camera camera;
    @Getter private int bufferSize;
    private InputStream stream;
    private List<VideoFeedListener> videoFeedListeners;

    private boolean processing = true;

    public VideoFeedService(Camera camera, int bufferSize) throws IOException {
        this.camera = camera;
        this.bufferSize = bufferSize;
        this.videoFeedListeners = new ArrayList<>();

        // Parse the camera URL and open a connection
        URL url = new URL(camera.getLocalStreamUrl());
        log.info(String.format("Opening connection for %s: %s", camera.getName(), url));
        URLConnection connection = url.openConnection();

        // Set the camera stream
        this.stream = connection.getInputStream();
    }

    public VideoFeedService(Camera camera) throws IOException {
        this(camera, 512);
    }

    public void addListener(VideoFeedListener listener) {
        videoFeedListeners.add(listener);
    }

    public void removeListener(VideoFeedListener listener) {
        videoFeedListeners.remove(listener);
    }

    private synchronized void stop() {
        processing = false;
    }

    @Override
    public void run() {
        byte[] buffer;
        
        while (processing) {
            try {
                buffer = stream.readNBytes(bufferSize);

                // Send buffer to all listeners and kill dead listeners
                Iterator<VideoFeedListener> it = videoFeedListeners.iterator();
                while (it.hasNext()) {
                    VideoFeedListener listener = it.next();
                    if (listener.isAlive()) {
                        listener.process(buffer);
                    } else {
                        it.remove();
                    }
                }
            } catch (IOException e) {
                log.severe("Connection to " + camera.getLocalStreamUrl() + " was lost");
                stop();
            }
        }

        try {
            stream.close();
        } catch (IOException e) {
            log.severe("Could not close stream for " + camera.getLocalStreamUrl());
        }
    }
}
