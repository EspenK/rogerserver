package me.kverna.roger.server.controller;

import lombok.extern.java.Log;
import me.kverna.roger.server.VideoFeedResponseBody;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller for forwarding a video feed from a camera
 */
@Log
@ApiController("/api/video")
public class VideoFeedController {

    private final CameraService cameraService;

    public VideoFeedController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @GetMapping("/{camera}")
    public ResponseEntity<VideoFeedResponseBody> cameraVideoFeed(@PathVariable("camera") String cameraName, HttpServletResponse response) {
        Camera camera = cameraService.findCamera(cameraName);

        response.setContentType("multipart/x-mixed-replace; boundary=FRAME");

        VideoFeedResponseBody stream = new VideoFeedResponseBody();
        cameraService.addConnection(camera, stream);

        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

    @GetMapping("/{camera}/info")
    public Camera cameraInfo(@PathVariable("camera") String cameraName) {
        return cameraService.findCamera(cameraName);
    }
}
