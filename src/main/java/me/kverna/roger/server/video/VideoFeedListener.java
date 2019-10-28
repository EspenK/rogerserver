package me.kverna.roger.server.video;

public interface VideoFeedListener {
    void process(byte[] chunk);

    boolean isAlive();
}
