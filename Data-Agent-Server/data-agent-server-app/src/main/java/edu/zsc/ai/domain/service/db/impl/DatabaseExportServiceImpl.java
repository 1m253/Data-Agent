package edu.zsc.ai.domain.service.db.impl;

import edu.zsc.ai.domain.service.db.ConnectionService;
import edu.zsc.ai.domain.service.db.DatabaseExportService;
import edu.zsc.ai.plugin.capability.DatabaseProvider;
import edu.zsc.ai.plugin.manager.DefaultPluginManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseExportServiceImpl implements DatabaseExportService {

    private final ConnectionService connectionService;

    @Override
    public String exportDatabaseDdl(Long connectionId, String databaseName, Long userId) {
        // Open connection without specifying catalog/schema to keep current database context
        connectionService.openConnection(connectionId, null, null, userId);

        ConnectionManager.ActiveConnection active = ConnectionManager.getOwnedConnection(connectionId, null, null, userId);

        DatabaseProvider provider = DefaultPluginManager.getInstance().getDatabaseProviderByPluginId(active.pluginId());

        // Get full database export (includes tables, views, triggers, functions, procedures)
        String dbDdl = provider.exportDatabaseDdl(active.connection(), databaseName);

        // Prepend header
        StringBuilder script = new StringBuilder();
        script.append("-- Database: ").append(databaseName).append("\n\n");
        script.append(dbDdl);

        return script.toString();
    }

    @Override
    public List<String> exportAllTableDdls(Long connectionId, String databaseName, Long userId) {
        connectionService.openConnection(connectionId, null, null, userId);

        ConnectionManager.ActiveConnection active = ConnectionManager.getOwnedConnection(connectionId, null, null, userId);

        DatabaseProvider provider = DefaultPluginManager.getInstance().getDatabaseProviderByPluginId(active.pluginId());

        return provider.exportAllTableDdls(active.connection(), databaseName);
    }
}
