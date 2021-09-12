package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDConverter extends Converter<UUID> {
    public boolean overridesLength() {
        return true;
    }

    public String getDefaultCharset() {
        return "utf8mb4";
    }

    public String getDefaultCollation() {
        return "utf8mb4_unicode_ci";
    }

    public int getDefaultLength() {
        return 36;
    }

    public int getDecimalPlaces() {
        return 0;
    }

    public String getSqlType() {
        return "CHAR";
    }

    public UUID deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return UUID.fromString(resultSet.getString(columnName));
    }

    public Object serialize(UUID value) {
        return value.toString();
    }
}
