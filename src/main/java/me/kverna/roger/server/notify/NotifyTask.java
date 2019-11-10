package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Webhook;
import me.kverna.roger.server.data.WebhookUrl;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class NotifyTask implements Runnable {

    private Webhook webhook;
    private List<WebhookUrl> webhookUrls;

    public NotifyTask(Webhook webhook, List<WebhookUrl> webhookUrls) {
        this.webhook = webhook;
        this.webhookUrls = webhookUrls;
    }

    @Override
    public void run() {
        for (WebhookUrl webhookUrl : webhookUrls) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(webhookUrl.getUrl(), webhook, Webhook.class);
        }
    }
}
