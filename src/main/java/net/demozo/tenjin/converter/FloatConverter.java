package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatConverter extends Converter<Float> {
    public boolean overridesLength() {
        return true;
    }

    public String getDefaultCharset() {
        return null;
    }

    public String getDefaultCollation() {
        return null;
    }

    public int getDefaultLength() {
        return 10;
    }

    public int getDecimalPlaces() {
        return 0;
    }

    public String getSqlType() {
        return "FLOAT";
    }

    public Float deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getFloat(columnName);
    }

    public Object serialize(Float value) {
        return value.toString();
    }
}
