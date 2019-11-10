package me.kverna.roger.server.service;

import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.repository.WebhookUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class WebhookUrlService {

    private WebhookUrlRepository repository;

    @Autowired
    public WebhookUrlService(WebhookUrlRepository repository) {
        this.repository = repository;
    }

    public void createWebhookUrl(WebhookUrl webhookUrl) {
        repository.save(webhookUrl);
    }

    public List<WebhookUrl> getAllWebhookUrl() {
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
}
