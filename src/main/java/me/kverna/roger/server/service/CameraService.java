package me.kverna.roger.server.service;

import me.kverna.roger.server.VideoCaptureService;
import me.kverna.roger.server.VideoFeedListener;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.CameraRepository;
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
    private Map<Camera, VideoCaptureService> captureServices;

    public CameraService(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
        this.captureServices = new HashMap<>();
    }

    public void setCaptureService(Camera camera, VideoCaptureService service) {
        captureServices.put(camera, service);
    }

    public void addConnection(Camera camera, VideoFeedListener listener) {
        captureServices.get(camera).addListener(listener);
    }

    public void removeConnection(Camera camera, VideoFeedListener listener) {
        captureServices.get(camera).removeListener(listener);
    }

    public Camera findCamera(String name) {
        Optional<Camera> camera = cameraRepository.findByName(name);
        if (camera.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found with name " + name);
        }

        return camera.get();
    }

    public List<Camera> findAllCameras() {
        return cameraRepository.findAll();
    }
}
