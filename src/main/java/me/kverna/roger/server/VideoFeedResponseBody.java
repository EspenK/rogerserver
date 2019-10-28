package me.kverna.roger.server;

import lombok.ToString;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@ToString
public class VideoFeedResponseBody implements StreamingResponseBody, VideoFeedListener {

    private BlockingQueue<byte[]> queue;
    private boolean running = true;

    public VideoFeedResponseBody() {
        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void process(byte[] chunk) {
        try {
            queue.put(chunk);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeTo(OutputStream outputStream) {
        try {
            while (running) {
                outputStream.write(queue.take());
            }
        } catch (InterruptedException | IOException ignored) {
        } finally {
            System.out.println("Stopping write");
            running = false;
        }
    }

    @Override
    public boolean isAlive() {
        return running;
    }
}
