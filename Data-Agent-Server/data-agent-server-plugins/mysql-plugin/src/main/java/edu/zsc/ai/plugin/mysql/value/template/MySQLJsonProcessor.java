package edu.zsc.ai.plugin.mysql.value.template;

import java.sql.SQLException;

import edu.zsc.ai.plugin.value.DefaultValueProcessor;
import edu.zsc.ai.plugin.value.JdbcValueContext;

/**
 * MySQL JSON type processor.
 * MySQL 5.7+ supports native JSON type.
 *
 * @author hhz
 * @date 2025-11-14
 */
public class MySQLJsonProcessor extends DefaultValueProcessor {

    @Override
    public Object convertJdbcValueByType(JdbcValueContext context) throws SQLException {
        // JSON is stored as string in JDBC
        return context.getResultSet().getString(context.getColumnIndex());
    }
}
