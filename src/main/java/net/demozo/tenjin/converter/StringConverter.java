package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringConverter extends Converter<String> {
    public boolean overridesLength() {
        return false;
    }

    public String getDefaultCharset() {
        return "utf8mb4";
    }

    public String getDefaultCollation() {
        return "utf8mb4_unicode_ci";
    }

    public int getDefaultLength() {
        return 191;
    }

    public int getDecimalPlaces() {
        return 0;
    }

    public String getSqlType() {
        return null;
    }

    public String deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    public Object serialize(String value) {
        return value;
    }
}
