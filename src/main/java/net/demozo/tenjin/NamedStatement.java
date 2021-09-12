package net.demozo.tenjin;

import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NamedStatement {
    private final Connection connection;
    private final HashMap<String, Object> parameters;
    private StringBuilder stringBuilder;
    private String query;

    public NamedStatement(DataSource source, String query) throws SQLException {
        parameters = new HashMap<>();
        this.query = query;
        connection = source.getConnection();
    }

    public NamedStatement(DataSource source, StringBuilder stringBuilder) throws SQLException {
        parameters = new HashMap<>();
        this.stringBuilder = stringBuilder;
        connection = source.getConnection();
    }

    /**
     * Set a named parameter.
     * @see PreparedStatement#setObject(int, Object)
     */
    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /**
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        PreparedStatement statement = getStatement();
        var result = statement.executeQuery();

        statement.close();
        connection.close();

        return result;
    }

    /**
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        PreparedStatement statement = getStatement();
        var result = statement.executeUpdate();

        statement.close();
        connection.close();

        return result;
    }

    private PreparedStatement getStatement() throws SQLException {
        String sql = query == null ? stringBuilder.toString() : query;
        Pattern pattern = Pattern.compile(":([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(sql);
        List<Object> params = new ArrayList<>();

        while(matcher.find()) {
            String paramName = matcher.group();

            sql = sql.replaceFirst(String.format("%s", paramName), "?");
            params.add(parameters.get(paramName.substring(1)));
        }

        PreparedStatement statement = connection.prepareStatement(sql);

        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);

            statement.setString(i + 1, param == null ? null : param.toString());
        }

        return statement;
    }
}
