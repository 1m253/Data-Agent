package edu.zsc.ai.domain.service.db;

public interface DatabaseImportService {

    /**
     * Execute SQL script for import
     *
     * @param connectionId connection ID
     * @param sqlScript SQL script to execute
     * @param userId user ID
     */
    void executeSqlScript(Long connectionId, String sqlScript, Long userId);
}
