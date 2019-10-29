package me.kverna.roger.server.video;

import lombok.ToString;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A StreamingResponseBody implementation that uses data provided by
 * a VideoFeedService using VideoFeedListener.
 * <p>
 * After creation, it should be added as a listener to the desired
 * VideoFeedService. When the OutputStream in `process` is closed,
 * `isAlive` will return false.
 */
@ToString
public class VideoFeedResponseBody implements StreamingResponseBody, VideoFeedListener {

    private BlockingQueue<byte[]> queue;
    private boolean running = true;

    public VideoFeedResponseBody() {
        queue = new LinkedBlockingQueue<>();
    }

    /**
     * Puts the given data from the VideoFeedService into a queue, which
     * can then be handled by `writeTo`.
     *
     * @param chunk a chunk of data using the buffer size specified by the VideoFeedService
     */
    @Override
    public void process(byte[] chunk) {
        try {
            queue.put(chunk);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes all data from the queue to the OutputStream. It will stop
     * when the OutputStream is closed.
     *
     * @param outputStream the stream to pass data to
     */
    @Override
    public void writeTo(OutputStream outputStream) {
        try {
            while (running) {
                outputStream.write(queue.take());
            }
        } catch (InterruptedException | IOException ignored) {
        } finally {
            stop();
        }
    }

    /**
     * Returns true when data is still being streamed.
     *
     * @return true when data is still being streamed
     */
    @Override
    public boolean isAlive() {
        return running;
    }

    /**
     * Stops the response stream.
     */
    private synchronized void stop() {
        running = false;
    }
}
