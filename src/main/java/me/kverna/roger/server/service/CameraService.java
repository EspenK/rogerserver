package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.repository.CameraRepository;
import me.kverna.roger.server.video.VideoDetectionTask;
import me.kverna.roger.server.video.VideoFeedListener;
import me.kverna.roger.server.video.VideoFeedTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for handling of Camera instances.
 *
 * This service has general repository functions, and handles executing the
 * background video stream tasks for each Camera instance. Background
 * tasks are executed automatically when cameras are created, and can
 * be connected to using `addConnection`. Stopping of a service is handled
 * upon deletion of a Camera using `removeCamera`.
 */
@Service
public class CameraService {

    private CameraRepository cameraRepository;
    private Map<Camera, VideoFeedTask> captureTasks;
    private TaskExecutor serviceExecutor;
    private NotifyService notifyService;

    public CameraService(CameraRepository cameraRepository, NotifyService notifyService, @Qualifier("serviceExecutor") TaskExecutor serviceExecutor) {
        this.cameraRepository = cameraRepository;
        this.captureTasks = new HashMap<>();
        this.serviceExecutor = serviceExecutor;
        this.notifyService = notifyService;
    }

    /**
     * Executes a background task for the given camera and assigns it for handling listeners.
     *
     * @param camera the camera to start the new task with
     */
    public void startCaptureTask(Camera camera) throws MalformedURLException {
        VideoFeedTask task = new VideoFeedTask(camera);
        captureTasks.put(camera, task);
        serviceExecutor.execute(task);
    }

    /**
     * Assign a listener to the background task for the given camera.
     *
     * @param camera the camera that has the desired task
     * @param listener the new listener to assign to the camera's task
     */
    public void addConnection(Camera camera, VideoFeedListener listener) {
        captureTasks.get(camera).addListener(listener);
    }

    public void startDetectionTask(Camera camera) {
        VideoDetectionTask task = new VideoDetectionTask(camera, notifyService.getNotifier());
        addConnection(camera, task);
        serviceExecutor.execute(task);
    }

    /**
     * Create a new Camera entity, and start a background task for it.
     *
     * @param camera the new Camera instance
     * @return the new Camera instance after saving to the repository
     */
    public Camera addCamera(Camera camera) {
        Optional<Camera> existing = cameraRepository.findByHostAndPort(camera.getHost(), camera.getPort());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("A camera with host %s and port %d already exists", camera.getHost(), camera.getPort()));
        }

        camera = cameraRepository.save(camera);

        // Start a background task for the new camera
        try {
            startCaptureTask(camera);
        } catch (MalformedURLException e) {
            // FIXME: at this point, no transactions to the database should be performed
            cameraRepository.delete(camera);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The generated URL %s is malformed", camera.getLocalStreamUrl()));
        }

        notifyService.getNotifier().notify(camera.getName(), "A new camera was added");

        return camera;
    }

    /**
     * Find a Camera using the given id.
     *
     * @param id the id associated with the Camera
     * @return the found Camera
     */
    public Camera findCamera(int id) {
        Optional<Camera> camera = cameraRepository.findById(id);
        if (camera.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found with id " + id);
        }

        return camera.get();
    }

    /**
     * Remove a Camera with the given id.
     *
     * @param id the id of the Camera to remove
     */
    public void removeCamera(int id) {
        Camera camera = findCamera(id);

        // Stop and remove the capture service
        captureTasks.get(camera).stop();
        captureTasks.remove(camera);

        cameraRepository.delete(camera);
    }

    /**
     * Return a list of all cameras in the repository.
     *
     * @return a list of all cameras in the repository
     */
    public List<Camera> findAllCameras() {
        return cameraRepository.findAll();
    }
}
