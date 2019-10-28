package me.kverna.roger.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Camera {

    @Id @GeneratedValue private Integer id;
    @NonNull private String name;
    @NonNull private String host;
    @NonNull private String description;

    @JsonIgnore
    public String getLocalStreamUrl() {
        int port = 8080;  // TODO: add configuration for camera port

        return String.format("http://%s:%d/stream.mjpg", host, port);
    }
}
