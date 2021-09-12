package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanConverter extends Converter<Boolean> {
    @Override
    public boolean overridesLength() {
        return false;
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
        return "bool";
    }

    @Override
    public Boolean deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBoolean(columnName);
    }

    @Override
    public Object serialize(Boolean value) {
        return value;
    }
}
