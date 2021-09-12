package net.demozo.tenjin;

import java.util.Objects;

public final class WhereClause {
    private final String column;
    private final String operator;
    private final Object value;
    private final boolean and;

    public WhereClause(String column, String operator, Object value, boolean and) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.and = and;
    }

    public String column() {
        return column;
    }

    public String operator() {
        return operator;
    }

    public Object value() {
        return value;
    }

    public boolean and() {
        return and;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WhereClause) obj;
        return Objects.equals(this.column, that.column) &&
                Objects.equals(this.operator, that.operator) &&
                Objects.equals(this.value, that.value) &&
                this.and == that.and;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, operator, value, and);
    }

    @Override
    public String toString() {
        return "WhereClause[" +
                "column=" + column + ", " +
                "operator=" + operator + ", " +
                "value=" + value + ", " +
                "and=" + and + ']';
    }

}
