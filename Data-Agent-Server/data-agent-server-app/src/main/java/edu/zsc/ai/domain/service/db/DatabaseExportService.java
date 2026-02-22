package edu.zsc.ai.domain.service.db;

import java.util.List;

public interface DatabaseExportService {

    /**
     * Export database DDL (CREATE DATABASE + all table DDLs)
     *
     * @param connectionId connection ID
     * @param databaseName database name
     * @param userId user ID
     * @return DDL script
     */
    String exportDatabaseDdl(Long connectionId, String databaseName, Long userId);

    /**
     * Export all table DDLs in a database
     *
     * @param connectionId connection ID
     * @param databaseName database name
     * @param userId user ID
     * @return list of CREATE TABLE statements
     */
    List<String> exportAllTableDdls(Long connectionId, String databaseName, Long userId);
}
