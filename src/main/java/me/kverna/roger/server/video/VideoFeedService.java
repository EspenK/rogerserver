package me.kverna.roger.server.video;

import lombok.Getter;
import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    private boolean running = true;

    public VideoFeedService(Camera camera, int bufferSize) throws IOException {
        this.camera = camera;
        this.bufferSize = bufferSize;
        this.videoFeedListeners = new ArrayList<>();
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
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            // Attempt to open a connection
            log.info(String.format("Opening connection for %s: %s", camera.getName(), camera.getLocalStreamUrl()));
            try {
                openConnectionUntilConnected(60);
            } catch (InterruptedException e) {
                stop();
            }

            // Serve data to all listeners
            serveStreamForever();
            log.severe("Connection to " + camera.getLocalStreamUrl() + " was lost");

            // Close the stream
            try {
                stream.close();
            } catch (IOException e) {
                log.severe("Could not close stream for " + camera.getLocalStreamUrl());
            }
        }
    }

    /**
     * Opens a connection to the camera associated with the VideoFeedService.
     * It keeps retrying until a connection has been established
     *
     * @param sleepSeconds the time to sleep between each connection in seconds
     * @throws InterruptedException when the connection is interrupted
     */
    private void openConnectionUntilConnected(long sleepSeconds) throws InterruptedException {
        while (true) {
            try {
                openConnection();
                break;
            } catch (IOException e) {
                log.warning(String.format("Could not open connection to %s. Retrying in %d seconds.", camera.getLocalStreamUrl(), sleepSeconds));
                Thread.sleep(sleepSeconds * 1000);
            }
        }
    }

    /**
     * Opens a connection to the camera associated with the VideoFeedService.
     *
     * @throws IOException if the URL can't be opened
     */
    private void openConnection() throws IOException {
        // Parse the camera URL
        URL url;
        try {
            url = new URL(camera.getLocalStreamUrl());
        } catch (MalformedURLException e) {
            // Generate a runtime exception for unexpected malformed url, which should not be possible to reach
            throw new RuntimeException("Camera " + camera + " has malformed URL where it should not be possible.");
        }

        // Open a connection to the camera stream
        URLConnection connection = url.openConnection();

        // Set the camera stream
        this.stream = connection.getInputStream();
    }

    /**
     * Read the camera stream and serve it to all listeners forever, or
     * until the stream is closed unexpectedly.
     */
    private void serveStreamForever() {
        byte[] buffer;
        boolean serving = true;

        while (serving) {
            try {
                buffer = stream.readNBytes(bufferSize);
                sendToListeners(buffer);
            } catch (IOException ignored) {
                serving = false;
            }
        }
    }

    /**
     * Send the given chunk to every listener. Any dead listeners will be removed.
     *
     * @param chunk a byte[] of data to be sent to every listener
     */
    private void sendToListeners(byte[] chunk) {
        Iterator<VideoFeedListener> it = videoFeedListeners.iterator();
        while (it.hasNext()) {
            VideoFeedListener listener = it.next();
            if (listener.isAlive()) {
                listener.process(chunk);
            } else {
                it.remove();
            }
        }
    }
}
