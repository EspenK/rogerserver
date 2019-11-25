package me.kverna.roger.server.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Capture {

    @Id @GeneratedValue private long id;
    private String cameraName;
    @Lob
    private byte[] frame;
    @Temporal(TemporalType.TIMESTAMP) private Date timestamp;
}
