package net.demozo.tenjin.test.models;

import net.demozo.tenjin.HasTimestamps;
import net.demozo.tenjin.Model;
import net.demozo.tenjin.ReferenceCollection;
import net.demozo.tenjin.RelationshipType;
import net.demozo.tenjin.annotation.Column;
import net.demozo.tenjin.annotation.PrimaryKey;
import net.demozo.tenjin.annotation.Relationship;
import net.demozo.tenjin.annotation.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table(name = "users")
public class User extends Model<UUID> implements HasTimestamps {
    @Column
    @PrimaryKey
    protected UUID id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @Relationship(columnName = "author_id", type = RelationshipType.HasMany)
    private ReferenceCollection<BlogPost, UUID> posts;

    public User() {}

    public User(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<BlogPost> getPosts() {
        return posts.fetch();
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void create() {
        this.createdAt = Instant.now();
    }

    @Override
    public void update() {
        this.updatedAt = Instant.now();
    }
}
