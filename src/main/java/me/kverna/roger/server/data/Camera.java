package me.kverna.roger.server.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Camera {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private String host;
}









