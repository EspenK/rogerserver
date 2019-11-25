package me.kverna.roger.server.data.webhook;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Builder
@Data
public class Webhook {

    private String username;
    private String avatar_url;
    private String content;
    @Singular private List<Embed> embeds;
}
