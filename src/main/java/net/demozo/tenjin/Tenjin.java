package net.demozo.tenjin;

import net.demozo.tenjin.annotation.Column;
import net.demozo.tenjin.annotation.PrimaryKey;
import net.demozo.tenjin.annotation.Relationship;
import net.demozo.tenjin.annotation.Table;
import net.demozo.tenjin.converter.Converter;
import net.demozo.tenjin.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Tenjin {
    private static boolean isDebugEnabled = false;
    static DataSource source;
    static Converters converters;
    static Logger logger = LoggerFactory.getLogger(Tenjin.class);

    public static void init(DataSource source) {
        if (converters == null) {
            converters = new Converters();
        }

        Tenjin.source = source;
        info("Initialized Tenjin.");
    }

    public static void shutdown() {
        try {
            tryShutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tryShutdown() throws IOException {
        info("Closing DataSource.");

        if (source instanceof Closeable) {
            ((Closeable) source).close();
        }

        info("Shutdown Tenjin.");
    }

    public static void setDebugEnabled(boolean enabled) {
        isDebugEnabled = enabled;
    }

    /**
     * Register a new {@link Converter}.
     *
     * @param clazz     The {@link Class} to register the {@link Converter} for.
     * @param converter The {@link Converter} to register.
     */
    public static void registerConverter(Class<?> clazz, Converter<?> converter) {
        if (converters == null) {
            converters = new Converters();
        }

        var existed = converters.containsKey(clazz);

        converters.put(clazz, converter);

        if (existed) {
            logger.warn("Overwrote converter for {} with {}.", clazz.getName(), converter.getClass().getSimpleName());
        } else {
            info("Registered {} converter for {}.", converter.getClass().getSimpleName(), clazz.getSimpleName());
        }
    }

    /**
     * Saves and then fetches the saved model from the database, properly hydrating it's relationships.
     *
     * @param model The model to be created.
     * @return The created model.
     */
    public static <T extends Model<K>, K> T create(T model) {
        var modelClass = model.getClass();
        var primaryField = getPrimaryField(modelClass);

        info("Creating model for class {}.", modelClass.getName());
        save(model);

        try {
            var accessible = primaryField.canAccess(model);
            primaryField.setAccessible(true);

            T dbModel = (T) Tenjin.get(modelClass, primaryField.get(model));
            primaryField.setAccessible(accessible);

            return dbModel;
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        throw new ModelSavingException("Creating new model was unsuccessful.");
    }

    /**
     * Saves a model to the database.
     *
     * @param model The model to be saved.
     * @return Whether or not the procedure was successful.
     */
    public static <T extends Model<K>, K> boolean save(T model) {
        try {
            var modelClass = model.getClass();
            var table = Optional.of(modelClass.getAnnotation(Table.class))
                    .orElseThrow(() -> new InvalidTableException(String.format("%s does not have a Table annotation.", modelClass.getName())));
            var query = new StringBuilder("INSERT INTO ").append(table.value()).append(" VALUES(");
            var stmt = new NamedStatement(source, query);

            List<Field> fields = getAllFieldsWithColumns(modelClass);

            if (model instanceof HasTimestamps timestamps) {

                if (timestamps.getCreatedAt() == null) {
                    timestamps.create();
                }

                timestamps.update();
            }

            for (var field : fields) {
                var name = getFieldName(field);
                var fieldClass = field.getType();
                var isAccessible = field.canAccess(model);
                var converter = (Converter) converters.get(fieldClass);
                var column = field.getAnnotation(Column.class);

                field.setAccessible(true);
                var value = field.get(model);

                if(fieldClass.isEnum()) {
                    query.append(":")
                            .append(name)
                            .append(", ");

                    stmt.setParameter(name, value);
                    continue;
                }

                if (field.isAnnotationPresent(Relationship.class)) {
                    var relationship = field.getAnnotation(Relationship.class);

                    if (relationship.type() != RelationshipType.BelongsToOne && relationship.type() != RelationshipType.BelongsToMany) {
                        continue;
                    }

                    var collectionType = (ParameterizedType) field.getGenericType();
                    var referencedKeyClass = (Class<?>) collectionType.getActualTypeArguments()[1];

                    converter = converters.get(referencedKeyClass);

                    if (fieldClass.isAssignableFrom(Reference.class) || fieldClass.isAssignableFrom(ReferenceCollection.class)) {
                        var getKey = fieldClass.getMethod("getKey");
                        value = getKey.invoke(field.get(model));
                    } else {
                        throw new IllegalRelationshipException(String.format("Relationship annotation is not allowed on %s in class %s.", field.getName(), modelClass.getName()));
                    }
                }

                field.setAccessible(isAccessible);

                if(converter == null) {
                    throw new IllegalStateException("Converter is null for field %s of type %s.".formatted(field.getName(), field.getType()));
                }

                query.append(":")
                        .append(name)
                        .append(", ");

                stmt.setParameter(name, column.autoIncrements() ? null : converter.serialize(value));
            }

            query.delete(query.length() - 2, query.length())
                    .append(") ON DUPLICATE KEY UPDATE ");

            for (var field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    continue;
                }

                if (field.isAnnotationPresent(Relationship.class)) {
                    var relationship = field.getAnnotation(Relationship.class);

                    if (relationship.type() != RelationshipType.BelongsToOne && relationship.type() != RelationshipType.BelongsToMany) {
                        continue;
                    }
                }

                var name = getFieldName(field);

                query.append(name)
                        .append("=:")
                        .append(name)
                        .append(", ");
            }

            query.delete(query.length() - 2, query.length());

            info(query.toString());

            var rowCount = stmt.executeUpdate();

            /*
             * ON DUPLICATE KEY UPDATE returns a status code, not the row count.
             * 0 = No values on an existing row were updated.
             * 1 = A new row was inserted.
             * 2 = An existing row was inserted.
             */
            var savedProperly = rowCount >= 0 && rowCount <= 2;

            if (!savedProperly) {
                throw new ModelSavingException(String.format("Model %s was unable to be properly saved. Update returned %s.", modelClass.getName(), rowCount));
            }

            return true;
        } catch (SQLException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    /**
     * Performs a <code>SELECT * FROM table;</code> query.
     *
     * @param clazz The model class to get all objects for.
     * @return All models.
     */
    public static <T extends Model<K>, K> List<T> getAll(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new InvalidTableException(String.format("%s does not have a Table annotation.", clazz.getName()));
        }

        try {
            var table = clazz.getAnnotation(Table.class);
            var query = String.format("SELECT * FROM %s", table.value());
            var conn = source.getConnection();
            var stmt = conn.prepareStatement(query);

            info("Executing query: {}", query);

            var resultSet = stmt.executeQuery();
            var models = new ArrayList<T>();

            while (resultSet.next()) {
                models.add(hydrateModel(clazz, resultSet));
            }

            conn.close();

            return models;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Gets a relationship.
     *
     * @param clazz The model class of the relationship to get.
     * @param key   The key to find the relationship by.
     * @param joins The join clauses.
     * @return A list of retrieved relationship models.
     */
    public static <T extends Model<K>, K> List<T> getJoin(Class<T> clazz, K key, JoinClause... joins) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new InvalidTableException(String.format("%s does not have a Table annotation.", clazz.getName()));
        }

        try {
            var table = clazz.getAnnotation(Table.class);
            var query = new StringBuilder(String.format("SELECT * FROM %s", table.value()));

            for (var join : joins) {
                var joinTable = join.table().getAnnotation(Table.class);

                if (joinTable == null) {
                    throw new InvalidTableException(String.format("%s does not have a Table annotation.", join.table().getName()));
                }

                query.append(" JOIN ")
                        .append(joinTable.value())
                        .append(" ON ")
                        .append(table.value())
                        .append(".")
                        .append(join.columnB())
                        .append(" ")
                        .append(join.operator())
                        .append(" ")
                        .append(joinTable.value())
                        .append(".")
                        .append(join.columnA());
            }

            query.append(" WHERE ")
                    .append(table.value())
                    .append(".")
                    .append(getFieldName(getPrimaryField(clazz)))
                    .append("=?");

            var conn = source.getConnection();
            var stmt = conn.prepareStatement(query.toString());
            var value = getPrimaryConverter(clazz).serialize(key);

            info("Setting value in position 1 to {}", value);
            stmt.setObject(1, value);
            info("Executing query: {}", query.toString());

            var resultSet = stmt.executeQuery();
            var models = new ArrayList<T>();

            while (resultSet.next()) {
                models.add(hydrateModel(clazz, resultSet));
            }

            conn.close();

            return models;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Performs a select query with where clause.
     *
     * @param clazz  The model class to get.
     * @param wheres The where clauses.
     * @return All matching models.
     */
    public static <T extends Model<K>, K> List<T> getWhere(Class<T> clazz, WhereClause... wheres) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new InvalidTableException(String.format("%s does not have a Table annotation.", clazz.getName()));
        }

        try {
            var table = clazz.getAnnotation(Table.class);
            var query = new StringBuilder(String.format("SELECT * FROM %s WHERE ", table.value()));
            var firstWhere = true;

            for (var where : wheres) {
                if (!firstWhere) {
                    query.append(" AND ");
                }

                query.append(where.column())
                        .append(where.operator())
                        .append("?");

                firstWhere = false;
            }

            var conn = source.getConnection();
            var stmt = conn.prepareStatement(query.toString());

            for (var i = 0; i < wheres.length; i++) {
                var where = wheres[i];
                var converter = (Converter) converters.get(where.value().getClass());
                var value = converter.serialize(where.value());

                info("Setting value in position {} to {}", i + 1, value);
                stmt.setObject(i + 1, value);
            }

            info("Executing query: {}", query.toString());

            var resultSet = stmt.executeQuery();
            var models = new ArrayList<T>();

            info("Hydrating model in where");
            while (resultSet.next()) {
                models.add(hydrateModel(clazz, resultSet));
            }

            resultSet.close();
            stmt.close();
            conn.close();

            return models;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Gets a single model by key.
     *
     * @param clazz The model class.
     * @param key   The key to get the model by.
     * @return The retrieved model.
     */
    public static <T extends Model<K>, K> T get(Class<T> clazz, K key) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new InvalidTableException(String.format("%s does not have a Table annotation.", clazz.getName()));
        }

        try {
            var table = clazz.getAnnotation(Table.class);
            var query = String.format("SELECT * FROM %s WHERE id=?", table.value());
            var conn = source.getConnection();
            var stmt = conn.prepareStatement(query);

            stmt.setObject(1, getPrimaryConverter(clazz).serialize(key));
            info("Executing query: {}", query);

            var resultSet = stmt.executeQuery();

            if(!resultSet.first()) {
                stmt.close();
                conn.close();
                return null;
            }

            conn.close();

            return hydrateModel(clazz, resultSet);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private static <A extends Model<AKey>, AKey, B extends Model<BKey>, BKey> A hydrateModel(Class<A> clazz, ResultSet resultSet) {
        try {
            info("Hydrating model(s) of class {}", clazz.getName());

            var fields = getAllFieldsWithColumnOrRelationship(clazz);
            var model = (A) clazz.getDeclaredConstructors()[0].newInstance();

            for (Field field : fields) {
                info("Hydrating field {}", field.getName());

                var fieldType = field.getType();
                var converter = converters.get(fieldType);
                var accessible = field.canAccess(model);

                field.setAccessible(true);

                if (field.isAnnotationPresent(Relationship.class)) {
                    var relationship = field.getAnnotation(Relationship.class);

                    info("Hydrating relationship for field {}", field.getName());

                    if (field.getType().isAssignableFrom(Reference.class)) {
                        var collectionType = (ParameterizedType) field.getGenericType();
                        var referencedClass = (Class<B>) collectionType.getActualTypeArguments()[0];
                        var referencedKeyClass = (Class<B>) collectionType.getActualTypeArguments()[1];
                        var pkConverter = getPrimaryConverter(clazz);
                        var constructor = Reference.class.getDeclaredConstructors()[1];

                        var key = switch (relationship.type()) {
                            case BelongsToOne -> converters.get(referencedKeyClass).deserialize(resultSet, getFieldName(field));
                            case HasOne -> pkConverter.deserialize(resultSet, getFieldName(getPrimaryField(clazz)));
                            default -> {
                                throw new InvalidRelationshipTypeException(String.format("Relationship type %s is invalid on ReferenceCollections.", relationship.type()));
                            }
                        };


                        var reference = (Reference<B, BKey>) constructor.newInstance(
                                clazz,
                                referencedClass,
                                pkConverter,
                                relationship.value(),
                                key,
                                relationship.type()
                        );

                        field.set(model, reference);
                    } else if (field.getType().isAssignableFrom(ReferenceCollection.class)) {
                        var collectionType = (ParameterizedType) field.getGenericType();
                        var referencedClass = (Class<B>) collectionType.getActualTypeArguments()[0];
                        var referencedKeyClass = (Class<B>) collectionType.getActualTypeArguments()[1];
                        var pkConverter = getPrimaryConverter(clazz);
                        var constructor = ReferenceCollection.class.getDeclaredConstructors()[1];

                        var key = switch (relationship.type()) {
                            case BelongsToMany -> converters.get(referencedKeyClass).deserialize(resultSet, getFieldName(field));
                            case HasMany -> pkConverter.deserialize(resultSet, getFieldName(getPrimaryField(clazz)));
                            default -> {
                                throw new InvalidRelationshipTypeException(String.format("Relationship type %s is invalid on ReferenceCollections.", relationship.type()));
                            }
                        };

                        var collection = (ReferenceCollection<B, BKey>) constructor.newInstance(
                                clazz,
                                referencedClass,
                                pkConverter,
                                relationship.value(),
                                key,
                                relationship.type()
                        );

                        field.set(model, collection);
                    }
                } else {
                    if (fieldType.isEnum()) {
                        field.set(model, Enum.valueOf((Class<Enum>) fieldType, resultSet.getString(getFieldName(field))));
                    } else {
                        field.set(model, converter.deserialize(resultSet, getFieldName(field)));
                    }
                }

                field.setAccessible(accessible);
            }

            return model;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static <T extends Model<K>, K> Converter<K> getPrimaryConverter(Class<T> clazz) {
        return (Converter<K>) converters.get(getPrimaryField(clazz).getType());
    }

    static <T extends Model<K>, K> Field getPrimaryField(Class<T> clazz) {
        var fields = getAllFieldsWithColumnOrRelationship(clazz);

        return fields.stream().filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .findFirst().orElseThrow(() -> new PrimaryKeyException(String.format("No field found annotated with PrimaryKey annotation on class %s", clazz.getName())));
    }

    private static <T extends Model<K>, K> List<Field> getAllFieldsWithColumnOrRelationship(Class<T> clazz) {
        List<Field> fields = new ArrayList<>();

        Class<?> parent = clazz;

        while (parent != Object.class) {
            Field[] declaredFields = parent.getDeclaredFields();
            parent = parent.getSuperclass();

            fields.addAll(Arrays.asList(declaredFields));
        }

        fields = fields.stream().filter(field -> field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Relationship.class)).toList();

        return fields;
    }

    private static <T extends Model<K>, K> List<Field> getAllFieldsWithColumns(Class<T> clazz) {
        return getAllFieldsWithColumnOrRelationship(clazz).stream()
                .filter(field -> field.isAnnotationPresent(Column.class)).toList();
    }

    static String getFieldName(Field field) {
        var column = field.getAnnotation(Column.class);

        if (column.value().isEmpty()) {
            var name = kebabCase(field.getName());

            if (field.isAnnotationPresent(Relationship.class)) {
                return name + "_id";
            }

            return name;
        } else {
            return column.value();
        }
    }

    private static String kebabCase(String string) {
        return string.replaceAll("([A-Z])", "_$1").toLowerCase(Locale.ROOT);
    }

    static void info(String message, Object... parameters) {
        if (isDebugEnabled) {
            logger.info(message, parameters);
        }
    }
}
