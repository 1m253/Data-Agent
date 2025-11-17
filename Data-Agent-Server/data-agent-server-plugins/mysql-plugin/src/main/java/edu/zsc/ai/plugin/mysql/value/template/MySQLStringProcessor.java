package edu.zsc.ai.plugin.mysql.value.template;

import java.sql.SQLException;

import edu.zsc.ai.plugin.value.DefaultValueProcessor;
import edu.zsc.ai.plugin.value.JdbcValueContext;

/**
 * Processor for MySQL CHAR/VARCHAR types.
 *
 * @author hhz
 * @date 2025-11-15
 */
public class MySQLStringProcessor extends DefaultValueProcessor {
    @Override
    public Object convertJdbcValueByType(JdbcValueContext context) throws SQLException {
        return context.getResultSet().getString(context.getColumnIndex());
    }
}
