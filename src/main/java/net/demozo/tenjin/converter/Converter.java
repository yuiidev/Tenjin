package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Converter<T> {
    public abstract boolean overridesLength();
    public abstract String getDefaultCharset();
    public abstract String getDefaultCollation();
    public abstract int getDefaultLength();
    public abstract int getDecimalPlaces();
    public abstract String getSqlType();
    public abstract T deserialize(ResultSet resultSet, String columnName) throws SQLException;
    public abstract Object serialize(T value);
}
