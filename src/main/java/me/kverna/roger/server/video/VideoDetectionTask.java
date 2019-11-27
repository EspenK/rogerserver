package me.kverna.roger.server.video;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.notify.Notifier;
import net.sf.jipcam.axis.MjpegFrame;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.CvMemStorage;
import org.bytedeco.opencv.opencv_core.CvPoint;
import org.bytedeco.opencv.opencv_core.CvPoint2D32f;
import org.bytedeco.opencv.opencv_core.CvPoint3D32f;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.CvSeq;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bytedeco.opencv.global.opencv_core.cvClearMemStorage;
import static org.bytedeco.opencv.global.opencv_core.cvCreateImage;
import static org.bytedeco.opencv.global.opencv_core.cvGetSeqElem;
import static org.bytedeco.opencv.global.opencv_core.cvGetSize;
import static org.bytedeco.opencv.global.opencv_core.cvPointFrom32f;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_AA;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_HOUGH_GRADIENT;
import static org.bytedeco.opencv.global.opencv_imgproc.cvCircle;
import static org.bytedeco.opencv.global.opencv_imgproc.cvCvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.cvHoughCircles;
import static org.bytedeco.opencv.global.opencv_imgproc.cvSmooth;

/**
 * A long-lived task for detecting circles in an attached video feed task.
 * <p>
 * In the future, this could use some interface implementation to handle
 * different kinds of detection.
 */
public class VideoDetectionTask implements VideoFeedListener, Runnable {

    private static final int DETECTION_TIMEOUT_FRAMES = 24 * 5;
    private OpenCVFrameConverter.ToIplImage iplImageConverter;
    private Java2DFrameConverter frameConverter;

    private BlockingQueue<MjpegFrame> frames;
    private boolean running = true;
    private int framesSinceDetection;

    private Camera camera;
    private Notifier notifier;
    private CvMemStorage storage;

    /**
     * Create a task for detecting video frames from the given camera.
     * The notifier must be supplied in order to send notifications
     * when detections are made.
     *
     * @param camera the camera the detection applies to
     * @param notifier a notifier to send notifications to
     */
    public VideoDetectionTask(Camera camera, Notifier notifier) {
        this.frames = new LinkedBlockingQueue<>(20);
        this.framesSinceDetection = 1;
        this.storage = CvMemStorage.create();
        this.iplImageConverter = new OpenCVFrameConverter.ToIplImage();
        this.frameConverter = new Java2DFrameConverter();

        this.camera = camera;
        this.notifier = notifier;
    }

    /**
     * Puts the given frame from the VideoFeedTask into a queue, which
     * can then be handled by the running thread.
     *
     * @param frame an MJPEG frame of video
     */
    @Override
    public void process(MjpegFrame frame) {
        try {
            frames.put(frame);
        } catch (InterruptedException e) {
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
    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // Decode the next frame in the queue for performing OpenCV operations
                Mat mat = imdecode(new Mat(new BytePointer(frames.take().getJpegBytes()), false), IMREAD_COLOR);
                IplImage image = new IplImage(mat);

                // Detect all and any circles in the frame
                CvSeq circles = detectCircles(image);

                if (circles.total() > 0) {

                    // If circles are detected, only notify if it has been DETECTION_TIMEOUT_FRAMES
                    // since the last detection
                    if (framesSinceDetection > 0) {
                        applyCircles(image, circles);
                        notifier.notify(camera, ":bell: Circle detected.", convertToJpeg(image));
                        notifier.buzz(camera, true);
                    }

                    // Update the detection timeout
                    framesSinceDetection = -DETECTION_TIMEOUT_FRAMES;

                    // The timeout is complete when the frame detection timeout reaches 0 (it goes from negative)
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

    /**
     * Detect circles in the given image using Hough Circles algorithm.
     *
     * @param image the image to detect circles in.
     * @return the sequence of detected circles
     */
    private CvSeq detectCircles(IplImage image) {
        cvClearMemStorage(storage);

        // Initialize grayscale image with 1 channel
        IplImage gray = cvCreateImage(cvGetSize(image), image.depth(), 1);

        // Paste the source image converted to grayscale
        cvCvtColor(image, gray, CV_BGR2GRAY);

        // Smooth out the edges
        cvSmooth(gray, gray);

        // Detect circles in the image
        return cvHoughCircles(gray, this.storage, CV_HOUGH_GRADIENT, 1, 100, 100, 80, 15, 500);
    }

    /**
     * Applies a sequence of circles to an image visually.
     *
     * @param image the image to apply circles to
     * @param circles the circles to apply
     */
    private void applyCircles(IplImage image, CvSeq circles) {
        for (int i = 0; i < circles.total(); i++) {
            CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));
            CvPoint center = cvPointFrom32f(new CvPoint2D32f(circle.x(), circle.y()));
            int radius = Math.round(circle.z());
            cvCircle(image, center, radius, CvScalar.GREEN, 6, CV_AA, 0);
        }
    }

    /**
     * Convert the given image to JPEG encoded data.
     *
     * @param image the image to convert
     * @return the JPEG encoded image
     */
    private byte[] convertToJpeg(IplImage image) {
        BufferedImage bufferedImage = frameConverter.getBufferedImage(iplImageConverter.convert(image));

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
