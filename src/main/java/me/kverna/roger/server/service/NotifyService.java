package me.kverna.roger.server.service;

import lombok.Getter;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.notify.Notifier;
import me.kverna.roger.server.repository.WebhookUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class NotifyService {

    private WebhookUrlRepository repository;
    @Getter private Notifier notifier;

    @Autowired
    public NotifyService(WebhookUrlRepository repository, @Qualifier("serviceExecutor") TaskExecutor serviceExecutor) {
        this.repository = repository;
        this.notifier = new Notifier(getAllWebhookUrls(), serviceExecutor);
    }

    public void createWebhookUrl(WebhookUrl webhookUrl) {
        webhookUrl = repository.save(webhookUrl);
        notifier.addWebhookUrl(webhookUrl);
    }

    public List<WebhookUrl> getAllWebhookUrls() {
        return repository.findAll();
    }

    public void deleteWebhookUrl(WebhookUrl webhookUrl) {
        repository.delete(webhookUrl);
        notifier.removeWebhookUrl(webhookUrl);
    }

    public WebhookUrl getWebhookUrl(Long id) {
        Optional<WebhookUrl> webhookUrl = repository.findById(id);
        if (webhookUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return webhookUrl.get();
    }
}
