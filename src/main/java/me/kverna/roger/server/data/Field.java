package me.kverna.roger.server.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Field {
    private String name;
    private String value;
    private boolean inline;
}
