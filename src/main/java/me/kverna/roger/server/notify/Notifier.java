package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.data.webhook.Webhook;

public interface Notifier {

    void notify(Webhook webhook);

    void notify(Camera camera, String description);

    void notify(Camera camera, String description, byte[] captureFrame);

    void notify(Webhook webhook, WebhookUrl webhookUrl);

    void buzz(Camera camera, boolean activate);
}
