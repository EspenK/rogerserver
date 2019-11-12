package me.kverna.roger.server.video;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.notify.Notifier;
import net.sf.jipcam.axis.MjpegFrame;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.CvMemStorage;
import org.bytedeco.opencv.opencv_core.CvSeq;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bytedeco.opencv.global.opencv_core.cvCreateImage;
import static org.bytedeco.opencv.global.opencv_core.cvGetSize;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_HOUGH_GRADIENT;
import static org.bytedeco.opencv.global.opencv_imgproc.cvCvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.cvHoughCircles;
import static org.bytedeco.opencv.global.opencv_imgproc.cvSmooth;

public class VideoDetectionTask implements VideoFeedListener, Runnable {

    private static final int DETECTION_TIMEOUT_SECONDS = 30;

    private BlockingQueue<MjpegFrame> frames;
    private boolean running = true;

    private long timeDetected;
    private boolean circleWasDetected = false;

    private Camera camera;
    private Notifier notifier;

    public VideoDetectionTask(Camera camera, Notifier notifier) {
        this.frames = new LinkedBlockingQueue<>();
        this.timeDetected = 0;

        this.camera = camera;
        this.notifier = notifier;
    }

    private boolean canDetect() {
        return timeDetected < (System.currentTimeMillis() - DETECTION_TIMEOUT_SECONDS * 1000);
    }

    @Override
    public void process(MjpegFrame frame) {
        if (!canDetect()) {
            return;
        }

        try {
            frames.put(frame);
        } catch (InterruptedException e) {
            stop();
        }
    }

    @Override
    public boolean isAlive() {
        return running;
    }

    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Mat mat = imdecode(new Mat(new BytePointer(frames.take().getJpegBytes()), false), IMREAD_COLOR);
                IplImage image = new IplImage(mat);

                int circles = detectCircles(image);
                if (circles > 0) {
                    // Notify when circles are detected, and was not previously detected
                    if (!circleWasDetected) {
                        notifier.notify(camera.getName(), "Circle detected.");
                        System.out.println(String.format("Detected %d circles!", circles));
                    }

                    // Update time of detection
                    timeDetected = System.currentTimeMillis();
                    circleWasDetected = true;

                    // Discard unneeded frames
                    frames.clear();
                } else if (circleWasDetected) {
                    // Notify when circles are no longer detected
                    notifier.notify(camera.getName(), ":crab: Circle is gone :crab:");
                    System.out.println("Circles are gone :crab:");
                    circleWasDetected = false;
                }
            }
        } catch (InterruptedException e) {
            stop();
        }
    }

    private int detectCircles(IplImage image) {
        // Initialize grayscale image with 1 channel
        IplImage gray = cvCreateImage(cvGetSize(image), image.depth(), 1);

        // Paste the source image converted to grayscale
        cvCvtColor(image, gray, CV_BGR2GRAY);

        // Smooth out the edges
        cvSmooth(gray, gray);

        // Detect circles in the image
        CvMemStorage storage = CvMemStorage.create();
        CvSeq circles = cvHoughCircles(gray, storage, CV_HOUGH_GRADIENT, 1, 100, 100, 100, 15, 500);

        return circles.total();
    }
}
