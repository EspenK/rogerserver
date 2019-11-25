package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.Embed;
import me.kverna.roger.server.data.Webhook;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.notify.BuzzerTask;
import me.kverna.roger.server.notify.Notifier;
import me.kverna.roger.server.notify.NotifyTask;
import me.kverna.roger.server.repository.WebhookUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

@Service
public class NotifyService implements Notifier {

    private WebhookUrlRepository repository;
    private TaskExecutor serviceExecutor;
    private String notifierUrl;

    @Autowired
    public NotifyService(WebhookUrlRepository repository, @Qualifier("serviceExecutor") TaskExecutor serviceExecutor, Environment environment) {
        this.repository = repository;
        this.serviceExecutor = serviceExecutor;

        String port = environment.getProperty("server.port");
        String host = InetAddress.getLoopbackAddress().getHostName();

        System.out.println(port + ", " + host);
        this.notifierUrl = String.format("http://%s:%s", host, port);
    }

    public void createWebhookUrl(WebhookUrl webhookUrl) {
        notifyWebhookUrlChanged("Webhook added", webhookUrl);
        repository.save(webhookUrl);
    }

    public List<WebhookUrl> getAllWebhookUrls() {
        return repository.findAll();
    }

    public void deleteWebhookUrl(WebhookUrl webhookUrl) {
        notifyWebhookUrlChanged("Webhook deleted", webhookUrl);
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
        notify(Webhook.builder()
                .embed(defaultEmbed().title(title).description(description).build())
                .build());
    }

    @Override
    public void notify(Webhook webhook, WebhookUrl webhookUrl) {
        serviceExecutor.execute(new NotifyTask(webhook, webhookUrl));
    }

    private void notifyWebhookUrlChanged(String title, WebhookUrl webhookUrl) {
        Webhook webhook = Webhook.builder()
                .embed(defaultEmbed().title(title).build())
                .build();

        notify(webhook, webhookUrl);
    }

    private Embed.EmbedBuilder defaultEmbed() {
        return Embed.builder().color(2003199).url(notifierUrl);
    }

    @Override
    public void buzz(Camera camera, boolean activate) {
        serviceExecutor.execute(new BuzzerTask(camera, activate));
    }
}
