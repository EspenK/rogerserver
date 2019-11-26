package me.kverna.roger.server.service;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.Capture;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.data.webhook.Embed;
import me.kverna.roger.server.data.webhook.Image;
import me.kverna.roger.server.data.webhook.Webhook;
import me.kverna.roger.server.notify.BuzzerTask;
import me.kverna.roger.server.notify.Notifier;
import me.kverna.roger.server.notify.NotifyTask;
import me.kverna.roger.server.repository.WebhookUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotifyService implements Notifier {

    private WebhookUrlRepository repository;
    private TaskExecutor serviceExecutor;
    private CaptureService captureService;

    @Value("${server.base_url}")
    private String baseUrl;

    private DateFormat dateFormat;

    @Autowired
    public NotifyService(WebhookUrlRepository repository, @Qualifier("serviceExecutor") TaskExecutor serviceExecutor, CaptureService captureService) {
        this.repository = repository;
        this.serviceExecutor = serviceExecutor;
        this.captureService = captureService;
        this.dateFormat = new SimpleDateFormat("MM-dd-yyyy-hh-mm-ss");
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
    public void notify(Camera camera, String description) {
        notify(Webhook.builder()
                .embed(defaultEmbed().title(camera.getName()).description(description).build())
                .build());
    }

    @Override
    public void notify(Camera camera, String description, byte[] captureFrame) {
        Webhook webhook = Webhook.builder()
                .embed(defaultEmbed().title(camera.getName()).description(description).build())
                .build();

        serviceExecutor.execute(new NotifyTask(webhook, getAllWebhookUrls(), hook -> {
            Capture capture = captureService.capture(camera, captureFrame);

            String imageUrl = String.format("%s/api/capture/%d.jpg?timestamp=%s", baseUrl, capture.getId(), dateFormat.format(capture.getTimestamp()));
            System.out.println(imageUrl);
            hook.getEmbeds().get(0).setImage(new Image(imageUrl));
        }));
    }

    @Override
    public void notify(Webhook webhook, WebhookUrl webhookUrl) {
        List<WebhookUrl> webhookUrls = new ArrayList<>();
        webhookUrls.add(webhookUrl);
        serviceExecutor.execute(new NotifyTask(webhook, webhookUrls));
    }

    private void notifyWebhookUrlChanged(String title, WebhookUrl webhookUrl) {
        Webhook webhook = Webhook.builder()
                .embed(defaultEmbed().title(title).build())
                .build();

        notify(webhook, webhookUrl);
    }

    private Embed.EmbedBuilder defaultEmbed() {
        return Embed.builder().color(2003199).url(baseUrl);
    }

    @Override
    public void buzz(Camera camera, boolean activate) {
        serviceExecutor.execute(new BuzzerTask(camera, activate));
    }
}
