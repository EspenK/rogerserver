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
    @NonNull private String host;
    @NonNull private int port;
    @NonNull private String name;
    @NonNull private String description;

    @JsonIgnore
    private String getLocalUrl() {
        return String.format("http://%s:%d/", host, port);
    }

    @JsonIgnore
    public String getLocalStreamUrl() {
        return getLocalUrl() + "stream.mjpg";
    }

    @JsonIgnore
    public String getBuzzerUrl(boolean activate) {
        return getLocalUrl() + (activate ? "alert" : "stop");
    }
}
