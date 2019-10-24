package me.kverna.roger.server.controller;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

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
    public StreamingResponseBody cameraVideoFeed(@PathVariable("camera") String cameraName) {
        Camera camera = cameraService.findCamera(cameraName);
        InputStream cameraStream = camera.getStream();

        return outputStream -> IOUtils.copy(cameraStream, outputStream);
    }

    @GetMapping("/{camera}/info")
    public Camera cameraInfo(@PathVariable("camera") String cameraName) {
        return cameraService.findCamera(cameraName);
    }
}

