package me.kverna.roger.server.data;

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

    public String getUrl() {
        // TODO: add configuration for port (and maybe protocol)
        int port = 8000;

        return String.format("http://%s:%d", host, port);
    }
}
