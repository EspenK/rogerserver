package me.kverna.roger.server.video;

/**
 * Interface for processing video feeds provided by VideoFeedService.
 */
public interface VideoFeedListener {
    /**
     * The VideoFeedService will send chunks of data to this function.
     * Processing can then be done in the implemented object.
     *
     * @param chunk a chunk of data using the buffer size specified by the VideoFeedService
     */
    void process(byte[] chunk);

    /**
     * Should return true when the listener is being used. When this is false,
     * the VideoFeedService will automatically unsubscribe from this listener.
     *
     * @return true when the listener is being used
     */
    boolean isAlive();
}
