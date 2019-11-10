package me.kverna.roger.server.controller;

import me.kverna.roger.server.annotation.Authorized;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.service.WebhookUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/webhooks")
public class WebhookUrlController {

    private WebhookUrlService webhookUrlService;

    @Autowired
    public WebhookUrlController(WebhookUrlService webhookUrlService) {
        this.webhookUrlService = webhookUrlService;
    }

    @Authorized
    @PostMapping
    public void createWebhookUrl(@RequestBody WebhookUrl webhookUrl) {
        webhookUrlService.createWebhookUrl(webhookUrl);
    }

    @Authorized
    @DeleteMapping("/{id}")
    public void deleteWebhookUrl(@PathVariable Long id) {
        webhookUrlService.deleteWebhookUrl(webhookUrlService.getWebhookUrl(id));
    }

    @Authorized
    @GetMapping("/{id}")
    public WebhookUrl getWebhookUrl(@PathVariable Long id) {
        return webhookUrlService.getWebhookUrl(id);
    }
}
