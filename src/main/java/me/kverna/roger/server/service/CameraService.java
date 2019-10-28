package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.CameraRepository;
import me.kverna.roger.server.video.VideoFeedListener;
import me.kverna.roger.server.video.VideoFeedService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CameraService {

    private CameraRepository cameraRepository;
    private Map<Camera, VideoFeedService> captureServices;

    public CameraService(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
        this.captureServices = new HashMap<>();
    }

    public void setCaptureService(Camera camera, VideoFeedService service) {
        captureServices.put(camera, service);
    }

    public void addConnection(Camera camera, VideoFeedListener listener) {
        captureServices.get(camera).addListener(listener);
    }

    public void removeConnection(Camera camera, VideoFeedListener listener) {
        captureServices.get(camera).removeListener(listener);
    }

    public Camera addCamera(Camera camera) {
        Optional<Camera> existing = cameraRepository.findByHostAndPort(camera.getHost(), camera.getPort());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("A camera with host %s and port %d already exists", camera.getHost(), camera.getPort()));
        }

        return cameraRepository.save(camera);
    }

    public Camera findCamera(int id) {
        Optional<Camera> camera = cameraRepository.findById(id);
        if (camera.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found with id " + id);
        }

        return camera.get();
    }

    public void removeCamera(int id) {
        cameraRepository.delete(findCamera(id));
    }

    public List<Camera> findAllCameras() {
        return cameraRepository.findAll();
    }
}
