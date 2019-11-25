package me.kverna.roger.server.video;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import net.sf.jipcam.axis.MjpegFrame;
import net.sf.jipcam.axis.MjpegInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Log
public class VideoFeedTask implements Runnable {

    /**
     * The time to wait between attempting to reconnect in seconds.
     */
    private static final long RECONNECT_SLEEP = 60;

    private Camera camera;
    private URL url;
    private List<VideoFeedListener> videoFeedListeners;

    private boolean running = true;

    public VideoFeedTask(Camera camera) throws MalformedURLException {
        this.camera = camera;
        this.url = new URL(camera.getLocalStreamUrl());
        this.videoFeedListeners = new ArrayList<>();
    }

    /**
     * Adds the given VideoFeedListener for output. This listener will be
     * automatically removed when its `isAlive` method returns false.
     *
     * @param listener the VideoFeedListener to add to this service
     */
    public void addListener(VideoFeedListener listener) {
        videoFeedListeners.add(listener);
    }

    /**
     * Stops the service.
     */
    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                log.info(String.format("Opening connection for %s: %s", camera.getName(), camera.getLocalStreamUrl()));
                MjpegInputStream mjpegInputStream = new MjpegInputStream(openStream());
                MjpegFrame frame;

                while (running && (frame = mjpegInputStream.readMjpegFrame()) != null) {
                    sendToListeners(frame);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info(String.format("Stopping %s: %s", camera.getName(), camera.getLocalStreamUrl()));

        // Stop all attached listeners
        for (VideoFeedListener listener : videoFeedListeners) {
            listener.stop();
        }
    }

    /**
     * Opens a connection to the camera associated with the VideoFeedTask,
     * and returns a stream when connected. It keeps retrying until a
     * connection has been established.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    private InputStream openStream() throws InterruptedException {
        while (true) {
            try {
                return url.openStream();
            } catch (IOException e) {
                log.warning(String.format("Could not open connection to %s. Retrying in %d seconds.", camera.getLocalStreamUrl(), RECONNECT_SLEEP));
                Thread.sleep(RECONNECT_SLEEP * 1000);
            }
        }
    }

    /**
     * Send the given chunk to every listener. Any dead listeners will be removed.
     *
     * @param frame an MJPEG frame to send to all listeners
     */
    private void sendToListeners(MjpegFrame frame) {
        Iterator<VideoFeedListener> it = videoFeedListeners.iterator();
        while (it.hasNext()) {
            VideoFeedListener listener = it.next();

            if (listener.isAlive()) {
                listener.process(frame);
            } else {
                it.remove();
            }
        }
    }
}
