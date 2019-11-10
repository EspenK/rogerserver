package me.kverna.roger.server.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Webhook {
    private String username;
    private String avatar_url;
    private String content;
    private List<Embed> embeds;
}
