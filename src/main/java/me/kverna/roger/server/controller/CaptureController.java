package me.kverna.roger.server.controller;

import me.kverna.roger.server.annotation.ApiController;
import me.kverna.roger.server.data.Capture;
import me.kverna.roger.server.service.CaptureService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@ApiController("/api/capture")
public class CaptureController {

    private CaptureService captureService;

    public CaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }

    @GetMapping("/{captureId}.jpg")
    public ResponseEntity<Resource> getCapturedFrame(@PathVariable long captureId) {
        Capture capture = captureService.findCapture(captureId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new ByteArrayResource(capture.getFrame()));
    }

    @GetMapping
    public List<Capture> getCaptureIds() {
        List<Capture> captures = captureService.findAllCaptures();
        captures.forEach(capture -> capture.setFrame(new byte[0]));
        return captures;
    }
}
