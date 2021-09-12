package net.demozo.tenjin.test.models;

import net.demozo.tenjin.Model;
import net.demozo.tenjin.ReferenceCollection;
import net.demozo.tenjin.RelationshipType;
import net.demozo.tenjin.annotation.Column;
import net.demozo.tenjin.annotation.Relationship;
import net.demozo.tenjin.annotation.Table;

import java.util.UUID;

@Table(name = "tags")
public class Tag extends Model<Integer> {
    @Column
    private String name;

    @Column
    private String slug;

    @Relationship(type = RelationshipType.HasMany)
    private ReferenceCollection<BlogPost, UUID> posts;
}
