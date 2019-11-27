package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.data.webhook.Webhook;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * A task for sending notifications to a Discord webhook.
 */
public class NotifyTask implements Runnable {

    private Webhook webhook;
    private List<WebhookUrl> webhookUrls;
    private PreExecute preExecute;

    /**
     * Create a task that will send a webhook object to the given
     * list of webhook URLs.
     * <p>
     * PreExecute will be performed at the start of the thread, and
     * allows for modification of the webhook before it should be sent.
     *
     * @param webhook     the webhook to send
     * @param webhookUrls the webhook URLs to send the webhook to
     * @param preExecute  custom execution that should be performed at the start of the thread
     */
    public NotifyTask(Webhook webhook, List<WebhookUrl> webhookUrls, PreExecute preExecute) {
        this.webhook = webhook;
        this.webhookUrls = webhookUrls;
        this.preExecute = preExecute;
    }

    /**
     * Create a task that will send a webhook object to the given
     * list of webhook URLs.
     *
     * @param webhook the webhook to send
     * @param webhookUrls the webhook URLs to send the webhook to
     */
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

    /**
     * An interface for running a piece of code that can
     * modify the given Webhook object.
     */
    public interface PreExecute {
        void execute(Webhook webhook);
    }
}
