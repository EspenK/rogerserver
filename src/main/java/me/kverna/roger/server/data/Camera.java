package me.kverna.roger.server.data;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Camera {

    private @Id @GeneratedValue Integer id;
    private @NonNull String name;
    private @NonNull String host;
}
