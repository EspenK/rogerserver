package me.kverna.roger.server.controller;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public void cameraVideoFeed(@PathVariable("camera") String cameraName, HttpServletResponse response) {
        Camera camera = cameraService.findCamera(cameraName);
        InputStream cameraStream = camera.getStream();

        response.setStatus(200);
        response.addHeader("Content-Type", "multipart/x-mixed-replace; boundary=FRAME");

        try {
            IOUtils.copy(cameraStream, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/{camera}/info")
    public Camera cameraInfo(@PathVariable("camera") String cameraName) {
        return cameraService.findCamera(cameraName);
    }
}
