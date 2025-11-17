package edu.zsc.ai.plugin.value;

import java.sql.SQLException;

/**
 * Value processor interface for handling data source-specific type conversions.
 * This interface separates type handling logic from data access logic.
 *
 * <p>This interface supports both JDBC and non-JDBC data sources through
 * a generic ValueContext abstraction.
 *
 * <p>Each data source type (JDBC, MongoDB, Redis, etc.) should provide
 * its own implementation to handle special types and conversions.
 *
 * @author hhz
 */
public interface ValueProcessor {

    /**
     * Extract and convert value using the provided context (generic method).
     *
     * <p>This is the primary method that works with any ValueContext implementation:
     * <ul>
     *   <li>JdbcValueContext for JDBC data sources</li>
     *   <li>MongoValueContext for MongoDB (future)</li>
     *   <li>RedisValueContext for Redis (future)</li>
     * </ul>
     *
     * @param context the context containing data source and column/field information
     * @return the extracted and converted value, may be null
     * @throws Exception if value extraction fails
     */
    default Object getValue(ValueContext context) throws Exception {
        // Default implementation: delegate to JDBC-specific method if context is JdbcValueContext
        if (context instanceof JdbcValueContext jdbcContext) {
            return getJdbcValue(jdbcContext);
        }
        throw new UnsupportedOperationException(
                "This processor does not support context type: " + context.getClass().getName());
    }

    /**
     * Extract and convert value from JDBC ResultSet using the provided context.
     *
     * <p>This is a JDBC-specific convenience method. For new implementations,
     * consider overriding the generic {@link #getValue(ValueContext)} method instead.
     *
     * @param context the JDBC context containing ResultSet and column information
     * @return the extracted and converted value, may be null
     * @throws SQLException if value extraction fails
     */
    Object getJdbcValue(JdbcValueContext context) throws SQLException;

    /**
     * Check if this processor supports the given context type.
     *
     * @param context the context to check
     * @return true if this processor can handle the context, false otherwise
     */
    default boolean supports(ValueContext context) {
        return context instanceof JdbcValueContext;
    }
}
