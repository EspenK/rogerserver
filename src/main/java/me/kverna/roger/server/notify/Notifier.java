package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.WebhookUrl;
import me.kverna.roger.server.data.webhook.Webhook;

/**
 * An interface for dealing with notifications required by
 * the ROGER system.
 */
public interface Notifier {

    /**
     * Send a notification to all webhook URLs with the given
     * Webhook object.
     *
     * @param webhook the webhook object to send
     */
    void notify(Webhook webhook);

    /**
     * Send a notification to all webhook URLs, using the
     * Camera's name as a title and a given description.
     *
     * @param camera      the camera the notification is for
     * @param description description of the notification
     */
    void notify(Camera camera, String description);

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
    void notify(Camera camera, String description, byte[] captureFrame);

    /**
     * Send a notification to the given webhook URL using
     * the given Webhook object.
     *
     * @param webhook the webhook object to send
     * @param webhookUrl the webhook URL to send to
     */
    void notify(Webhook webhook, WebhookUrl webhookUrl);


    void buzz(Camera camera, boolean activate);
}
