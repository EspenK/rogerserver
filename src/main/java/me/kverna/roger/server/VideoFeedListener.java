package me.kverna.roger.server;

public interface VideoFeedListener {
    void process(byte[] chunk);

    boolean isAlive();
}
