package me.kverna.roger.server.controller;

import me.kverna.roger.server.annotation.Authorized;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/webhooks")
public class WebhookUrlController {

    private NotifyService service;

    @Autowired
    public WebhookUrlController(NotifyService service) {
        this.service = service;
    }

    @Authorized
    @PostMapping
    public void createWebhookUrl(@RequestBody WebhookUrl webhookUrl) {
        service.createWebhookUrl(webhookUrl);
    }

    @Authorized
    @DeleteMapping("/{id}")
    public void deleteWebhookUrl(@PathVariable Long id) {
        service.deleteWebhookUrl(service.getWebhookUrl(id));
    }

    @Authorized
    @GetMapping
    public List<WebhookUrl> getAllWebhookUrl() {
        return service.getAllWebhookUrls();
    }

    @Authorized
    @GetMapping("/{id}")
    public WebhookUrl getWebhookUrl(@PathVariable Long id) {
        return service.getWebhookUrl(id);
    }
}
