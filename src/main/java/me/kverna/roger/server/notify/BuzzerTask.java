package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Camera;
import org.springframework.web.client.RestTemplate;

public class BuzzerTask implements Runnable {

    private String url;

    public BuzzerTask(Camera camera, boolean activate) {
        this.url = camera.getBuzzerUrl() + (activate ? "alert" : "stop");
    }

    @Override
    public void run() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(url, String.class);
    }
}
