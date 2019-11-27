package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.Capture;
import me.kverna.roger.server.repository.CaptureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for handling of frame captures in video detection.
 */
@Service
public class CaptureService {

    private CaptureRepository captureRepository;

    @Autowired
    public CaptureService(CaptureRepository captureRepository) {
        this.captureRepository = captureRepository;
    }

    /**
     * Store a new capture by the given camera with the given
     * encoded JPEG frame.
     *
     * @param camera    the camera the capture was taken with
     * @param jpegFrame the encoded JPEG frame to store
     * @return the saved Capture object
     */
    public Capture capture(Camera camera, byte[] jpegFrame) {
        Capture capture = new Capture();
        capture.setCameraName(camera.getName());
        capture.setTimestamp(new Date());
        capture.setFrame(jpegFrame);
        return captureRepository.save(capture);
    }

    /**
     * Return a list of all captures in the repository.
     *
     * @return a list of all captures in the repository.
     */
    public List<Capture> findAllCaptures() {
        return captureRepository.findAll();
    }

    /**
     * Find a capture with the given id.
     *
     * @param id the id of the capture to find
     * @return the found capture
     */
    public Capture findCapture(long id) {
        Optional<Capture> capture = captureRepository.findById(id);
        if (capture.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Capture not found with id " + id);
        }

        return capture.get();
    }

    /**
     * Delete the capture with the given id, if it exists.
     *
     * @param id the id of the capture to delete
     */
    public void deleteCapture(long id) {
        Capture capture = findCapture(id);
        captureRepository.delete(capture);
    }
}
