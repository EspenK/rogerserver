package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Embed;
import me.kverna.roger.server.data.Webhook;
import me.kverna.roger.server.data.WebhookUrl;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
import java.util.List;

public class Notifier {

    private List<WebhookUrl> webhookUrls;
    private TaskExecutor taskExecutor;

    public Notifier(List<WebhookUrl> webhookUrls, TaskExecutor taskExecutor) {
        this.webhookUrls = webhookUrls;
        this.taskExecutor = taskExecutor;
    }

    public void addWebhookUrl(WebhookUrl webhookUrl) {
        webhookUrls.add(webhookUrl);
    }

    public void removeWebhookUrl(WebhookUrl webhookUrl) {
        webhookUrls.remove(webhookUrl);
    }

    public void notify(Webhook webhook) {
        taskExecutor.execute(new NotifyTask(webhook, webhookUrls));
    }

    public void notify(String title, String description) {
        Webhook webhook = new Webhook();
        ArrayList<Embed> embeds = new ArrayList<>();

        Embed embed = new Embed();
        embed.setColor(2003199);
        embed.setTitle(title);
        embed.setDescription(description);
        embeds.add(embed);

        webhook.setEmbeds(embeds);

        notify(webhook);
    }
}
