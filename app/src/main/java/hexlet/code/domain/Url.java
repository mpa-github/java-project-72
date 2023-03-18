package hexlet.code.domain;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
//import java.time.LocalDateTime;

@Entity
public class Url extends Model {

    @Id
    private long id;
    private String name;
    //private LocalDateTime createdAt = LocalDateTime.now();
    private Instant createdAt = Instant.now();

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /*public LocalDateTime getCreatedAt() {
        return createdAt;
    }*/

    public Instant getCreatedAt() {
        return createdAt;
    }
}
