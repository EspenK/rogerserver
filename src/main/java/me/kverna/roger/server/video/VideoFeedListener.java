package me.kverna.roger.server.video;

import net.sf.jipcam.axis.MjpegFrame;

/**
 * Interface for processing video feeds provided by VideoFeedTask.
 */
public interface VideoFeedListener {
    /**
     * The VideoFeedTask will send chunks of data to this function.
     * Processing can then be done in the implemented object.
     *
     * @param frame a frame of video
     */
    void process(MjpegFrame frame);

    /**
     * Should return true when the listener is being used. When this is false,
     * the VideoFeedTask will automatically unsubscribe from this listener.
     *
     * @return true when the listener is being used
     */
    boolean isAlive();

    /**
     * Called when the attached task stops.
     */
    void stop();
}
