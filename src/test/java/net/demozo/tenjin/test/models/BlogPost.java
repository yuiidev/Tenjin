package net.demozo.tenjin.test.models;

import net.demozo.tenjin.Model;
import net.demozo.tenjin.Reference;
import net.demozo.tenjin.RelationshipType;
import net.demozo.tenjin.annotation.Column;
import net.demozo.tenjin.annotation.PrimaryKey;
import net.demozo.tenjin.annotation.Relationship;
import net.demozo.tenjin.annotation.Table;

import java.util.UUID;

@Table(name = "blog_posts")
public class BlogPost extends Model<UUID> {
    @Column
    @PrimaryKey
    protected UUID id;

    @Column
    private String title;

    @Column
    private String slug;

    @Column
    @Relationship(columnName = "author_id", type = RelationshipType.BelongsToOne)
    private Reference<User, UUID> author;

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public User getAuthor() {
        return author.fetch();
    }
}
