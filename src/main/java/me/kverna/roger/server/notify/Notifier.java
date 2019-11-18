package me.kverna.roger.server.notify;

import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.data.Webhook;

public interface Notifier {

    void notify(Webhook webhook);

    void notify(String title, String description);

    void buzz(Camera camera, boolean activate);
}
