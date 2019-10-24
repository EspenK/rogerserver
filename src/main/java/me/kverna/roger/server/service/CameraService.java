package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.CameraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CameraService {

    private CameraRepository cameraRepository;
    private Map<Camera, InputStream> streams;

    public CameraService(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
        streams = new HashMap<>();
    }

    public void setCameraStream(Camera camera, InputStream stream) {
        streams.put(camera, stream);
    }

    private Camera addStream(Camera camera) {
        if (streams.containsKey(camera)) {
            camera.setStream(streams.get(camera));
        }

        return camera;
    }

    public Camera findCamera(int id) {
        Optional<Camera> camera = cameraRepository.findById(id);
        if (camera.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found with id " + id);
        }

        return addStream(camera.get());
    }

    public Camera findCamera(String name) {
        Optional<Camera> camera = cameraRepository.findByName(name);
        if (camera.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found with name " + name);
        }

        return addStream(camera.get());
    }

    public List<Camera> findAllCameras() {
        return cameraRepository.findAll();
    }
}
