package edu.zsc.ai.controller;

import edu.zsc.ai.model.dto.request.ConnectRequest;
import edu.zsc.ai.model.dto.response.ApiResponse;
import edu.zsc.ai.model.dto.response.ConnectionTestResponse;
import edu.zsc.ai.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Connection Controller
 * Provides REST API endpoints for database connection management.
 *
 * @author Data-Agent
 * @since 0.0.1
 */
@Slf4j
@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {
    
    private final ConnectionService connectionService;
    
    /**
     * Test database connection without establishing persistent connection.
     * Returns detailed connection information including DBMS version, driver info, ping time, etc.
     *
     * @param request connect request
     * @return connection test response with detailed information
     */
    @PostMapping("/test")
    public ApiResponse<ConnectionTestResponse> testConnection(
            @Valid @RequestBody ConnectRequest request) {
        log.info("Testing connection: dbType={}, host={}, database={}", 
                request.getDbType(), request.getHost(), request.getDatabase());
        
        ConnectionTestResponse response = connectionService.testConnection(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Close an active database connection.
     *
     * @param connectionId unique connection identifier
     * @return success response
     */
    @DeleteMapping("/{connectionId}")
    public ApiResponse<Void> closeConnection(@PathVariable String connectionId) {
        log.info("Closing connection: connectionId={}", connectionId);
        
        connectionService.closeConnection(connectionId);
        return ApiResponse.success();
    }
}

