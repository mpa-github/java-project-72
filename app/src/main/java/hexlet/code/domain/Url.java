package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.Identity;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Url extends Model {

    @Id @Identity
    private long id;

    private String name;

    @WhenCreated // = Instant.now();
    private Instant createdAt;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
