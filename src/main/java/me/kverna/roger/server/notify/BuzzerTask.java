package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Camera;
import org.springframework.web.client.RestTemplate;

/**
 * A task for activating and deactivating the alert system
 * on the Camera devices.
 */
public class BuzzerTask implements Runnable {

    private String url;

    /**
     * Create a task that will activate or deactivate the alert
     * buzzer on the given camera.
     *
     * @param camera   the camera to alert
     * @param activate whether to activate or to deactivate the alert
     */
    public BuzzerTask(Camera camera, boolean activate) {
        this.url = camera.getBuzzerUrl(activate);
    }

    @Override
    public void run() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getForEntity(url, String.class);
    }
}
