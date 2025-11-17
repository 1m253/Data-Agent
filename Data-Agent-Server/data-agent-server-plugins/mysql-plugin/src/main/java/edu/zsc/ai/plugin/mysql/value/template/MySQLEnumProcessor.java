package edu.zsc.ai.plugin.mysql.value.template;

import java.sql.SQLException;

import edu.zsc.ai.plugin.value.DefaultValueProcessor;
import edu.zsc.ai.plugin.value.JdbcValueContext;

/**
 * Processor for MySQL ENUM type.
 *
 * @author hhz
 * @date 2025-11-15
 */
public class MySQLEnumProcessor extends DefaultValueProcessor {
    @Override
    public Object convertJdbcValueByType(JdbcValueContext context) throws SQLException {
        return context.getResultSet().getString(context.getColumnIndex());
    }
}
