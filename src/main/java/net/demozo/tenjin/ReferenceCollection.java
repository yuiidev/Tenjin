package net.demozo.tenjin;


import net.demozo.tenjin.converter.Converter;
import net.demozo.tenjin.exceptions.IllegalKeyModificationException;

import java.util.ArrayList;
import java.util.List;

import static net.demozo.tenjin.Tenjin.getFieldName;
import static net.demozo.tenjin.Tenjin.getPrimaryField;

public class ReferenceCollection<T extends Model<K>, K> {
    private final Class<?> referrerClass;
    private final Class<T> referencedClass;
    private final Converter<K> keyConverter;
    private final String columnName;
    private final RelationshipType relationshipType;
    private K key;
    private boolean isDirty;

    /**
     * Creates a new instance of a reference collection with it's relationship type set to BelongsToMany.
     * Used when creating a new record that has relationships that need to be populated during creation.
     * To prevent NullPointerExceptions use the value returned by {@link Tenjin#create(Model)} in the rest of the
     * application.
     *
     * @param key The key to set for this reference collection.
     */
    public ReferenceCollection(K key) {
        this.key = key;

        referrerClass = null;
        referencedClass = null;
        keyConverter = null;
        columnName = null;
        relationshipType = RelationshipType.BelongsToMany;
        isDirty = true;
    }

    public ReferenceCollection(Class<?> referrerClass, Class<T> referencedClass, Converter<K> keyConverter, String columnName, K key, RelationshipType relationshipType) {
        this.referrerClass = referrerClass;
        this.referencedClass = referencedClass;
        this.keyConverter = keyConverter;
        this.columnName = columnName;
        this.key = key;
        this.relationshipType = relationshipType;
    }

    public K getKey() {
        return key;
    }

    public List<T> fetch() {
        switch (relationshipType) {
            case HasMany -> {
                var where = new WhereClause(columnName, "=", keyConverter.serialize(key), true);
                return Tenjin.getWhere(referencedClass, where);
            }
            case BelongsToMany -> {
                var join = new JoinClause(referrerClass, getFieldName(getPrimaryField(referencedClass)), "=", columnName);
                return Tenjin.getJoin(referencedClass, key, join);
            }
        }

        return new ArrayList<>();
    }

    public void set(K key) {
        if (relationshipType == RelationshipType.BelongsToOne) {
            this.key = key;
            isDirty = true;
        } else {
            throw new IllegalKeyModificationException("Only relationships of type BelongsToOne can be modified.");
        }
    }

    public boolean isDirty() {
        return isDirty;
    }
}