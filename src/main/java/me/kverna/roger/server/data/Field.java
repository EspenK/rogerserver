package me.kverna.roger.server.data;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Field {

    private String name;
    private String value;
    private boolean inline;
}
