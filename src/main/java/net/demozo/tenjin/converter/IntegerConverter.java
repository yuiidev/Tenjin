package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerConverter extends Converter<Integer> {
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
        return 10;
    }

    @Override
    public int getDecimalPlaces() {
        return 0;
    }

    @Override
    public String getSqlType() {
        return "INT";
    }

    @Override
    public Integer deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }

    @Override
    public Object serialize(Integer value) {
        return value.toString();
    }
}
