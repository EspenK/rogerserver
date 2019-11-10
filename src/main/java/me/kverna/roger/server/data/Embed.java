package me.kverna.roger.server.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Embed {
    private long color;
    private String title;
    private String description;
    private List<Field> fields;
}
