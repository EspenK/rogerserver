package me.kverna.roger.server.video;

import lombok.ToString;
import net.sf.jipcam.axis.MjpegFrame;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A StreamingResponseBody implementation that uses data provided by
 * a VideoFeedTask using VideoFeedListener.
 *
 * After creation, it should be added as a listener to the desired
 * VideoFeedTask. When the OutputStream in `process` is closed,
 * `isAlive` will return false.
 */
@ToString
public class VideoFeedResponseBody implements StreamingResponseBody {

    private SharedFrame sharedFrame;

    /**
     * Create a new response body with a BlockingQueue with the capacity
     * of holding 20 video frames at once.
     */
    public VideoFeedResponseBody(SharedFrame sharedFrame) {
        this.sharedFrame = sharedFrame;
    }

    /**
     * Writes all video frames from the queue to the OutputStream.
     * It will stop when the OutputStream is closed.
     *
     * @param outputStream the stream to pass data to
     */
    @Override
    public void writeTo(OutputStream outputStream) {
        MjpegFrame frame;

        try {
            while (true) {
                frame = sharedFrame.receive();
                if (frame == null) {
                    return;
                }

                outputStream.write(frame.getBytes());
            }
        } catch (IOException ignored) {
        }
    }
}
