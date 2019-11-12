package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Embed;
import me.kverna.roger.server.data.Webhook;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.notify.Notifier;
import me.kverna.roger.server.notify.NotifyTask;
import me.kverna.roger.server.repository.WebhookUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotifyService implements Notifier {

    private WebhookUrlRepository repository;
    private TaskExecutor serviceExecutor;

    @Autowired
    public NotifyService(WebhookUrlRepository repository, @Qualifier("serviceExecutor") TaskExecutor serviceExecutor) {
        this.repository = repository;
        this.serviceExecutor = serviceExecutor;
    }

    public void createWebhookUrl(WebhookUrl webhookUrl) {
        repository.save(webhookUrl);
    }

    public List<WebhookUrl> getAllWebhookUrls() {
        return repository.findAll();
    }

    public void deleteWebhookUrl(WebhookUrl webhookUrl) {
        repository.delete(webhookUrl);
    }

    public WebhookUrl getWebhookUrl(Long id) {
        Optional<WebhookUrl> webhookUrl = repository.findById(id);
        if (webhookUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return webhookUrl.get();
    }

    @Override
    public void notify(Webhook webhook) {
        serviceExecutor.execute(new NotifyTask(webhook, getAllWebhookUrls()));
    }

    @Override
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
