package me.kverna.roger.server.data;

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
    @Singular private List<Field> fields;
}
