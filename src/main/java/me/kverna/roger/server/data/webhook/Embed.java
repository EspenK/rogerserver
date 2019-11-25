package me.kverna.roger.server.data.webhook;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Builder
@Data
public class Embed {

    private long color;
    private String title;
    private String description;
    private String url;
    private Image image;
    @Singular private List<Field> fields;
}
