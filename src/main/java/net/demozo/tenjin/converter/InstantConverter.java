package net.demozo.tenjin.converter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

public class InstantConverter extends Converter<Instant> {

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
        return "LONG";
    }

    @Override
    public Instant deserialize(ResultSet resultSet, String columnName) throws SQLException {
        return Instant.ofEpochMilli(resultSet.getLong(columnName));
    }

    @Override
    public Object serialize(Instant value) {
        return value.toEpochMilli();
    }
}
