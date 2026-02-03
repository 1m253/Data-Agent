package edu.zsc.ai.plugin.manager;

import edu.zsc.ai.plugin.Plugin;
import edu.zsc.ai.plugin.capability.ConnectionProvider;
import edu.zsc.ai.plugin.capability.ViewProvider;
import edu.zsc.ai.plugin.enums.DbType;
import edu.zsc.ai.plugin.driver.MavenCoordinates;

/**
 * Plugin Manager Utility Class
 * Provides static methods to access plugin management functionality.
 * Delegates to DefaultPluginManager singleton instance.
 *
 * @author Data-Agent
 * @since 0.0.1
 */
public class PluginManager {

    /**
     * Get the underlying plugin manager instance
     */
    private static IPluginManager getPluginManager() {
        return DefaultPluginManager.getInstance();
    }

    // ========== Plugin Query ==========

    /**
     * Find plugin by ID.
     *
     * @param pluginId plugin ID
     * @return plugin instance, or null if not found
     */
    public static Plugin findPluginById(String pluginId) {
        return getPluginManager().findPluginById(pluginId);
    }

    // ========== Plugin Selection ==========

    /**
     * Select the first plugin for a database type.
     * Returns the first plugin from the sorted list (plugins are sorted by version, newest first).
     *
     * @param dbTypeCode database type code (e.g., "mysql", "MYSQL")
     * @return selected plugin (first in sorted list)
     * @throws IllegalArgumentException if no plugin available for the database type
     */
    public static Plugin selectFirstPluginByDbType(String dbTypeCode) {
        return getPluginManager().selectFirstPluginByDbType(dbTypeCode);
    }

    /**
     * Select the most appropriate plugin for a database type based on database version.
     * Returns the plugin that best matches the database version.
     *
     * @param dbTypeCode      database type code (e.g., "mysql", "MYSQL")
     * @param databaseVersion actual database version from connection (e.g., "8.0.33")
     * @return selected plugin that best matches the database version
     * @throws IllegalArgumentException if no plugin available for the database type
     */
    public static Plugin selectPluginByDbTypeAndVersion(String dbTypeCode, String databaseVersion) {
        return getPluginManager().selectPluginByDbTypeAndVersion(dbTypeCode, databaseVersion);
    }

    // ========== Capability Selection ==========

    /**
     * Select ConnectionProvider for a specific database type.
     * Returns the first available plugin that implements ConnectionProvider capability.
     * Plugins are ordered by version (newest first).
     *
     * @param dbTypeCode database type code (e.g., "mysql", "MYSQL")
     * @return ConnectionProvider instance
     * @throws IllegalArgumentException if no plugin with ConnectionProvider capability found
     */
    public static ConnectionProvider selectConnectionProviderByDbType(String dbTypeCode) {
        return getPluginManager().selectConnectionProviderByDbType(dbTypeCode);
    }

    /**
     * Select ConnectionProvider by plugin ID.
     *
     * @param pluginId plugin ID
     * @return ConnectionProvider instance
     * @throws IllegalArgumentException if plugin not found or doesn't implement ConnectionProvider
     */
    public static ConnectionProvider selectConnectionProviderByPluginId(String pluginId) {
        return getPluginManager().selectConnectionProviderByPluginId(pluginId);
    }

    // ========== View Provider Selection ==========

    /**
     * Select ViewProvider for a specific database type.
     * Returns the first available plugin that implements ViewProvider capability.
     * Plugins are ordered by version (newest first).
     *
     * @param dbTypeCode database type code (e.g., "mysql", "MYSQL")
     * @return ViewProvider instance
     * @throws IllegalArgumentException if no plugin with ViewProvider capability found
     */
    public static ViewProvider selectViewProviderByDbType(String dbTypeCode) {
        return getPluginManager().selectViewProviderByDbType(dbTypeCode);
    }

    /**
     * Select ViewProvider by plugin ID.
     *
     * @param pluginId plugin ID
     * @return ViewProvider instance
     * @throws IllegalArgumentException if plugin not found or doesn't implement ViewProvider
     */
    public static ViewProvider selectViewProviderByPluginId(String pluginId) {
        return getPluginManager().selectViewProviderByPluginId(pluginId);
    }

    // ========== Driver Management ==========

    /**
     * Find Maven coordinates for a driver version.
     * Queries each plugin for the database type and returns coordinates from the first plugin that supports the version.
     *
     * @param dbType database type
     * @param driverVersion driver version (nullable)
     * @return Maven coordinates
     * @throws IllegalArgumentException if no plugin supports the version or database type not found
     */
    public static MavenCoordinates findDriverMavenCoordinates(DbType dbType, String driverVersion) {
        return getPluginManager().findDriverMavenCoordinates(dbType, driverVersion);
    }
}
