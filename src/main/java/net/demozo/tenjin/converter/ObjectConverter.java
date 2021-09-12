package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectConverter extends Converter<Object> {
    @Override
    public boolean overridesLength() {
        return true;
    }

    @Override
    public String getDefaultCharset() {
        return null;
    }

    @Override
    public String getDefaultCollation() {
        return null;
    }

    @Override
    public int getDefaultLength() {
        return 0;
    }

    @Override
    public int getDecimalPlaces() {
        return 0;
    }

    @Override
    public String getSqlType() {
        return "BLOB";
    }

    @Override
    public Object deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getObject(columnName);
    }

    @Override
    public Object serialize(Object value) {
        return value;
    }
}
