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

@Service
public class CaptureService {

    private CaptureRepository captureRepository;

    @Autowired
    public CaptureService(CaptureRepository captureRepository) {
        this.captureRepository = captureRepository;
    }

    public Capture capture(Camera camera, byte[] jpegFrame) {
        Capture capture = new Capture();
        capture.setCameraName(camera.getName());
        capture.setTimestamp(new Date());
        capture.setFrame(jpegFrame);
        return captureRepository.save(capture);
    }

    public List<Capture> getCaptures() {
        return captureRepository.findAll();
    }

    public Capture findCapture(long id) {
        Optional<Capture> capture = captureRepository.findById(id);
        if (capture.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Capture not found with id " + id);
        }

        return capture.get();
    }

    public void deleteCapture(long id) {
        Capture capture = findCapture(id);
        captureRepository.delete(capture);
    }
}
