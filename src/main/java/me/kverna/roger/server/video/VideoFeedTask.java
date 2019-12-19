package me.kverna.roger.server.video;

import lombok.Getter;
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

/**
 * Long-lived task that connects to a camera's video stream
 * and can relay the stream to other threads. It will stream
 * the frames to any listeners by sending them one-by-one.
 */
@Log
public class VideoFeedTask implements Runnable {

    /**
     * The time to wait between attempting to reconnect in seconds.
     */
    private static final long RECONNECT_SLEEP = 60;

    private Camera camera;
    private URL url;
    @Getter private SharedFrame sharedFrame;

    private boolean running = true;

    /**
     * Create a video feed task to retrieve video from the given camera.
     *
     * @param camera the camera to retrieve video from
     * @throws MalformedURLException if the camera's hostname or port is invalid
     */
    public VideoFeedTask(Camera camera) throws MalformedURLException {
        this.camera = camera;
        this.url = new URL(camera.getLocalStreamUrl());
        this.sharedFrame = new SharedFrame();
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

                // Stream all frames of video to every listener
                while (running && (frame = mjpegInputStream.readMjpegFrame()) != null) {
                    sharedFrame.send(frame);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info(String.format("Stopping %s: %s", camera.getName(), camera.getLocalStreamUrl()));

        // Show that the process is stopped by setting the frame to null
        sharedFrame.send(null);
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
}
