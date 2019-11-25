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

import static org.bytedeco.opencv.global.opencv_core.cvClearMemStorage;
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

    private static final int DETECTION_TIMEOUT_FRAMES = 24 * 5;

    private BlockingQueue<MjpegFrame> frames;
    private boolean running = true;

    private int framesSinceDetection;

    private Camera camera;
    private Notifier notifier;

    private CvMemStorage storage;

    public VideoDetectionTask(Camera camera, Notifier notifier) {
        this.frames = new LinkedBlockingQueue<>();
        this.framesSinceDetection = 1;
        this.storage = CvMemStorage.create();

        this.camera = camera;
        this.notifier = notifier;
    }

    @Override
    public void process(MjpegFrame frame) {
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
                byte[] frame = frames.take().getJpegBytes();
                Mat mat = imdecode(new Mat(new BytePointer(frame), false), IMREAD_COLOR);
                IplImage image = new IplImage(mat);

                int circles = detectCircles(image);

                if (circles > 0) {
                    if (framesSinceDetection > 0) {
                        notifier.notify(camera, ":bell: Circle detected.", frame);
                        notifier.buzz(camera, true);
                    }

                    framesSinceDetection = -DETECTION_TIMEOUT_FRAMES;
                } else if (framesSinceDetection == 0) {
                    notifier.notify(camera, ":no_bell: Circle disappeared.");
                    notifier.buzz(camera, false);
                }

                framesSinceDetection++;
            }
        } catch (InterruptedException e) {
            stop();
        }
    }

    private int detectCircles(IplImage image) {
        cvClearMemStorage(storage);

        // Initialize grayscale image with 1 channel
        IplImage gray = cvCreateImage(cvGetSize(image), image.depth(), 1);

        // Paste the source image converted to grayscale
        cvCvtColor(image, gray, CV_BGR2GRAY);

        // Smooth out the edges
        cvSmooth(gray, gray);

        // Detect circles in the image
        CvSeq circles = cvHoughCircles(gray, this.storage, CV_HOUGH_GRADIENT, 1, 100, 100, 80, 15, 500);

        return circles.total();
    }
}
