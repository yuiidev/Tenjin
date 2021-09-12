package net.demozo.tenjin;

import java.util.Objects;

public final class JoinClause {
    private final Class<?> table;
    private final String columnB;
    private final String operator;
    private final String columnA;

    public JoinClause(Class<?> table, String columnB, String operator, String columnA) {
        this.table = table;
        this.columnB = columnB;
        this.operator = operator;
        this.columnA = columnA;
    }

    public Class<?> table() {
        return table;
    }

    public String columnB() {
        return columnB;
    }

    public String operator() {
        return operator;
    }

    public String columnA() {
        return columnA;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JoinClause) obj;
        return Objects.equals(this.table, that.table) &&
                Objects.equals(this.columnB, that.columnB) &&
                Objects.equals(this.operator, that.operator) &&
                Objects.equals(this.columnA, that.columnA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, columnB, operator, columnA);
    }

    @Override
    public String toString() {
        return "JoinClause[" +
                "table=" + table + ", " +
                "columnB=" + columnB + ", " +
                "operator=" + operator + ", " +
                "columnA=" + columnA + ']';
    }


}
