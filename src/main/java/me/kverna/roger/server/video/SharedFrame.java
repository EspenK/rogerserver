package me.kverna.roger.server.video;

import net.sf.jipcam.axis.MjpegFrame;

public class SharedFrame {

    private MjpegFrame frame;

    public SharedFrame() {
        frame = null;
    }

    public synchronized void send(MjpegFrame frame) {
        this.frame = frame;
        notifyAll();
    }

    public synchronized MjpegFrame receive() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return frame;
    }
}
