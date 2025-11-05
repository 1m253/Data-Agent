package edu.zsc.ai.service;

import edu.zsc.ai.model.dto.request.ConnectRequest;
import edu.zsc.ai.model.dto.response.ConnectionTestResponse;

/**
 * Connection Service Interface
 * Provides database connection management operations.
 *
 * @author Data-Agent
 * @since 0.0.1
 */
public interface ConnectionService {
    
    /**
     * Test database connection without establishing persistent connection.
     * Returns detailed connection information including DBMS version, driver info, ping time, etc.
     *
     * @param request test connection request
     * @return connection test response with detailed information
     */
    ConnectionTestResponse testConnection(ConnectRequest request);
    
    /**
     * Close an active database connection and release resources.
     *
     * @param connectionId unique connection identifier
     */
    void closeConnection(String connectionId);
}

