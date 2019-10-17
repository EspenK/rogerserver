package me.kverna.roger.server.data;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Camera {

    @Id @GeneratedValue private Integer id;
    @NonNull private String name;
    @NonNull private String host;
}
