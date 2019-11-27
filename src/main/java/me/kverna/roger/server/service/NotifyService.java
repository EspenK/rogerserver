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

/**
 * Service layer for sending notifications using Discord webhooks.
 */
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

    /**
     * Add a new webhook URL for sending webhook objects to.
     *
     * @param webhookUrl the new webhook URL to add.
     */
    public void createWebhookUrl(WebhookUrl webhookUrl) {
        notifyWebhookUrlChanged("Webhook added", webhookUrl);
        repository.save(webhookUrl);
    }

    /**
     * Returns a list of all webhook URLs.
     *
     * @return a list of all webhook URLs.
     */
    public List<WebhookUrl> findAllWebhookUrls() {
        return repository.findAll();
    }

    /**
     * Find a webhook URL with the given id.
     *
     * @param id the id of the webhook URL to find
     * @return the found webhook URL
     */
    public WebhookUrl findWebhookUrl(Long id) {
        Optional<WebhookUrl> webhookUrl = repository.findById(id);
        if (webhookUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return webhookUrl.get();
    }

    /**
     * Delete the given webhook URL.
     *
     * @param id the id of the webhook URL to delete.
     */
    public void deleteWebhookUrl(long id) {
        WebhookUrl webhookUrl = findWebhookUrl(id);
        notifyWebhookUrlChanged("Webhook deleted", webhookUrl);
        repository.delete(webhookUrl);
    }

    /**
     * Send a notification to all webhook URLs with the given
     * Webhook object.
     *
     * @param webhook the webhook object to send
     */
    @Override
    public void notify(Webhook webhook) {
        serviceExecutor.execute(new NotifyTask(webhook, findAllWebhookUrls()));
    }

    /**
     * Send a notification to all webhook URLs, using the
     * Camera's name as a title and a given description.
     *
     * @param camera      the camera the notification is for
     * @param description description of the notification
     */
    @Override
    public void notify(Camera camera, String description) {
        notify(Webhook.builder()
                .embed(defaultEmbed().title(camera.getName()).description(description).build())
                .build());
    }

    /**
     * Send a notification to all webhook URLs, using the
     * Camera's name as a title and a given description, and
     * provide a JPEG image of a captured frame from the video
     * that should be saved to the database and passed in the
     * notification.
     *
     * @param camera the camera the notification is for
     * @param description description of the notification
     * @param captureFrame encoded JPEG image to save and send with the notification
     */
    @Override
    public void notify(Camera camera, String description, byte[] captureFrame) {
        Webhook webhook = Webhook.builder()
                .embed(defaultEmbed().title(camera.getName()).description(description).build())
                .build();

        serviceExecutor.execute(new NotifyTask(webhook, findAllWebhookUrls(), hook -> {
            Capture capture = captureService.capture(camera, captureFrame);

            String imageUrl = String.format("%s/api/capture/%d.jpg?timestamp=%s", baseUrl, capture.getId(), dateFormat.format(capture.getTimestamp()));
            System.out.println(imageUrl);
            hook.getEmbeds().get(0).setImage(new Image(imageUrl));
        }));
    }

    /**
     * Send a notification to the given webhook URL using
     * the given Webhook object.
     *
     * @param webhook the webhook object to send
     * @param webhookUrl the webhook URL to send to
     */
    @Override
    public void notify(Webhook webhook, WebhookUrl webhookUrl) {
        List<WebhookUrl> webhookUrls = new ArrayList<>();
        webhookUrls.add(webhookUrl);
        serviceExecutor.execute(new NotifyTask(webhook, webhookUrls));
    }

    /**
     * Alert the buzzer on the given camera.
     *
     * @param camera   the camera to alert
     * @param activate whether to activate or deactivate the alert buzzer
     */
    @Override
    public void buzz(Camera camera, boolean activate) {
        serviceExecutor.execute(new BuzzerTask(camera, activate));
    }

    /**
     * Create a simple notification that sends information about
     * a given webhook URL to that same URL.
     *
     * @param title the notification information
     * @param webhookUrl the webhook URL that was changed
     */
    private void notifyWebhookUrlChanged(String title, WebhookUrl webhookUrl) {
        Webhook webhook = Webhook.builder()
                .embed(defaultEmbed().title(title).build())
                .build();

        notify(webhook, webhookUrl);
    }

    /**
     * Create a simple Embed builder with a default blue color
     * and a URL to the server.
     *
     * @return the Embed builder
     */
    private Embed.EmbedBuilder defaultEmbed() {
        return Embed.builder().color(2003199).url(baseUrl);
    }
}
