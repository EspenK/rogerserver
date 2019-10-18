package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.CameraRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Controller for forwarding a video feed from a camera
 */
@Controller
@RequestMapping("video")
@Log
public class VideoFeedController {

    private final CameraRepository cameraRepository;

    public VideoFeedController(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
    }

    @GetMapping("{camera:.+}")
    public StreamingResponseBody cameraVideoFeed(@PathVariable("camera") String cameraName) throws IOException {
        // Attempt to find a camera object
        Camera camera = cameraRepository.findByName(cameraName);
        if (camera == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Camera not found");
        }

        // Attempt to get the video stream
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Resource> responseEntity;
        try {
            responseEntity = restTemplate.exchange(camera.getUrl(), HttpMethod.GET, null, Resource.class);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not access camera video feed.");
        }

        InputStream inputStream = Objects.requireNonNull(responseEntity.getBody()).getInputStream();

        return (outputStream -> readAndWrite(inputStream, outputStream));
    }

    private void readAndWrite(final InputStream is, OutputStream os) throws IOException {
        byte[] data = new byte[2048];
        int read;
        while ((read = is.read(data)) > 0) {
            os.write(data, 0, read);
        }
        os.flush();
    }
}

