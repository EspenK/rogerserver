package me.kverna.roger.server.controller;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.service.CameraService;
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
    public void cameraVideoFeed(@PathVariable("camera") String cameraName, HttpServletResponse response) {
        Camera camera = cameraService.findCamera(cameraName);

        log.info(String.format("Trying to connect to %s (%s)", camera.getName(), camera.getUrl()));

        /*RestTemplate restTemplate = new RestTemplate();
        response.setStatus(HttpStatus.OK.value());

        restTemplate.execute(
                camera.getUrl(),
                HttpMethod.GET,
                (ClientHttpRequest requestCallback) -> {
                },
                responseExtractor -> {
                    IOUtils.copy(responseExtractor.getBody(), response.getOutputStream());
                    return null;
                });*/
    }

    @GetMapping("/{camera}/info")
    public Camera cameraInfo(@PathVariable("camera") String cameraName) {
        return cameraService.findCamera(cameraName);
    }
}

