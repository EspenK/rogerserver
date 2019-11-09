package me.kverna.roger.server.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "\"user\"")
public class User {
    @Id @GeneratedValue private long id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NonNull private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NonNull private String password;
    @NonNull private String displayName;
}