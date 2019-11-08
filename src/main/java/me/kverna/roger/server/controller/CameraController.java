package me.kverna.roger.server.controller;

import lombok.extern.java.Log;
import me.kverna.roger.server.annotation.ApiController;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
import me.kverna.roger.server.video.VideoFeedResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller for forwarding a video feed from a camera
 */
@Log
@ApiController("/api/camera")
public class CameraController {

    private final CameraService cameraService;

    public CameraController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @GetMapping
    public List<Camera> getAllCameras() {
        return cameraService.findAllCameras();
    }

    @PostMapping
    public Camera addCamera(@RequestBody Camera camera) {
        return cameraService.addCamera(camera);
    }

    @GetMapping("/{camera}")
    public Camera getCamera(@PathVariable("camera") int cameraId) {
        return cameraService.findCamera(cameraId);
    }

    @DeleteMapping("/{camera}")
    public void deleteCamera(@PathVariable("camera") int cameraId) {
        cameraService.removeCamera(cameraId);
    }

    @GetMapping("/{camera}.mjpg")
    public ResponseEntity<VideoFeedResponseBody> cameraVideoFeed(@PathVariable("camera") int cameraId, HttpServletResponse response) {
        Camera camera = cameraService.findCamera(cameraId);

        // Create a response stream and add it to the associated background service
        VideoFeedResponseBody stream = new VideoFeedResponseBody();
        cameraService.addConnection(camera, stream);

        response.setContentType("multipart/x-mixed-replace; boundary=FRAME");
        return new ResponseEntity<>(stream, HttpStatus.OK);
    }
}
