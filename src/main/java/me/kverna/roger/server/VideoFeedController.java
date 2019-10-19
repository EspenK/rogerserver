package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.CameraRepository;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller for forwarding a video feed from a camera
 *
 * TODO: generify camera argument somehow
 */
@RestController
@RequestMapping("/video")
@Log
public class VideoFeedController {

    private final CameraRepository cameraRepository;

    public VideoFeedController(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
    }

    @GetMapping("/{camera}")
    public void cameraVideoFeed(@PathVariable("camera") String cameraName, HttpServletResponse response) throws IOException {
        // Attempt to find a camera object
        Camera camera = cameraRepository.findByName(cameraName);
        if (camera == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found");
        }

        log.info(String.format("Trying to connect to %s (%s)", camera.getName(), camera.getUrl()));

        RestTemplate restTemplate = new RestTemplate();
        response.setStatus(HttpStatus.OK.value());

        restTemplate.execute(
                camera.getUrl(),
                HttpMethod.GET,
                (ClientHttpRequest requestCallback) -> {
                },
                responseExtractor -> {
                    IOUtils.copy(responseExtractor.getBody(), response.getOutputStream());
                    return null;
                });
    }

    @GetMapping("/{camera}/info")
    public Camera cameraInfo(@PathVariable("camera") String cameraName) {
        // Attempt to find a camera object
        Camera camera = cameraRepository.findByName(cameraName);
        if (camera == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found");
        }

        return camera;
    }
}

