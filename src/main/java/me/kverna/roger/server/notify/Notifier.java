package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.Webhook;
import me.kverna.roger.server.data.WebhookUrl;

public interface Notifier {

    void notify(Webhook webhook);

    void notify(String title, String description);

    void notify(Webhook webhook, WebhookUrl webhookUrl);

    void buzz(Camera camera, boolean activate);
}
