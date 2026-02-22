package edu.zsc.ai.domain.service.db.impl;

import edu.zsc.ai.domain.service.db.ConnectionService;
import edu.zsc.ai.domain.service.db.DatabaseImportService;
import edu.zsc.ai.plugin.capability.DatabaseProvider;
import edu.zsc.ai.plugin.manager.DefaultPluginManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseImportServiceImpl implements DatabaseImportService {

    private final ConnectionService connectionService;

    @Override
    public void executeSqlScript(Long connectionId, String sqlScript, Long userId) {
        connectionService.openConnection(connectionId, null, null, userId);

        ConnectionManager.ActiveConnection active = ConnectionManager.getOwnedConnection(connectionId, null, null, userId);

        DatabaseProvider provider = DefaultPluginManager.getInstance().getDatabaseProviderByPluginId(active.pluginId());

        provider.executeSqlScript(active.connection(), sqlScript);

        log.info("SQL script executed successfully");
    }
}
