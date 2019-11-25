package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.data.webhook.Webhook;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class NotifyTask implements Runnable {

    private Webhook webhook;
    private List<WebhookUrl> webhookUrls;
    private PreExecute preExecute;

    public NotifyTask(Webhook webhook, List<WebhookUrl> webhookUrls, PreExecute preExecute) {
        this.webhook = webhook;
        this.webhookUrls = webhookUrls;
        this.preExecute = preExecute;
    }

    public NotifyTask(Webhook webhook, List<WebhookUrl> webhookUrls) {
        this(webhook, webhookUrls, null);
    }

    @Override
    public void run() {
        if (preExecute != null) {
            preExecute.execute(webhook);
        }

        RestTemplate restTemplate = new RestTemplate();

        for (WebhookUrl webhookUrl : webhookUrls) {
            restTemplate.postForObject(webhookUrl.getUrl(), webhook, Webhook.class);
        }
    }

    public interface PreExecute {
        void execute(Webhook webhook);
    }
}
