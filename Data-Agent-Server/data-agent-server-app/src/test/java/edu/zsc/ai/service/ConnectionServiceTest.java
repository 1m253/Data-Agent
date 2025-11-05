package edu.zsc.ai.service;

import edu.zsc.ai.exception.BusinessException;
import edu.zsc.ai.model.dto.request.ConnectRequest;
import edu.zsc.ai.model.dto.response.ConnectionTestResponse;
import edu.zsc.ai.model.enums.ConnectionTestStatus;
import edu.zsc.ai.plugin.Plugin;
import edu.zsc.ai.plugin.capability.ConnectionProvider;
import edu.zsc.ai.plugin.manager.PluginManager;
import edu.zsc.ai.plugin.model.ConnectionConfig;
import edu.zsc.ai.service.impl.ConnectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConnectionService
 *
 * @author Data-Agent
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {
    
    @Mock
    private PluginManager pluginManager;
    
    @Mock
    private Plugin mockPlugin;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private DatabaseMetaData mockMetaData;
    
    @InjectMocks
    private ConnectionServiceImpl connectionService;
    
    // Create a mock that implements both Plugin and ConnectionProvider
    private TestPlugin mockConnectionProvider;
    
    private ConnectRequest testRequest;
    
    @BeforeEach
    void setUp() {
        mockConnectionProvider = mock(TestPlugin.class);
        
        testRequest = ConnectRequest.builder()
                .dbType("MYSQL")
                .host("localhost")
                .port(3306)
                .database("testdb")
                .username("root")
                .password("password")
                .driverJarPath("/path/to/mysql-connector.jar")
                .timeout(30)
                .build();
    }
    
    // Helper interface for mocking both Plugin and ConnectionProvider
    interface TestPlugin extends Plugin, ConnectionProvider {
    }
    
    @Test
    void testConnection_Success() throws Exception {
        // Arrange
        when(pluginManager.getPluginsByDbTypeCode("MYSQL"))
                .thenReturn(java.util.List.of(mockConnectionProvider));
        when(mockConnectionProvider.connect(any(ConnectionConfig.class))).thenReturn(mockConnection);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("MySQL");
        when(mockMetaData.getDatabaseProductVersion()).thenReturn("8.0.43");
        when(mockMetaData.getDriverName()).thenReturn("MySQL Connector/J");
        when(mockMetaData.getDriverVersion()).thenReturn("mysql-connector-j-8.2.0");
        when(mockMetaData.getJDBCMajorVersion()).thenReturn(4);
        when(mockMetaData.getJDBCMinorVersion()).thenReturn(2);
        doNothing().when(mockConnectionProvider).closeConnection(mockConnection);
        
        // Act
        ConnectionTestResponse response = connectionService.testConnection(testRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(ConnectionTestStatus.SUCCEEDED, response.getStatus());
        assertTrue(response.getDbmsInfo().contains("MySQL"));
        assertTrue(response.getDbmsInfo().contains("8.0.43"));
        assertTrue(response.getDriverInfo().contains("MySQL Connector/J"));
        assertNotNull(response.getPing());
        assertTrue(response.getPing() >= 0);
        
        verify(pluginManager).getPluginsByDbTypeCode("MYSQL");
        verify(mockConnectionProvider).connect(any(ConnectionConfig.class));
        verify(mockConnectionProvider).closeConnection(mockConnection);
    }
    
    @Test
    void testConnection_NoPluginAvailable() {
        // Arrange
        when(pluginManager.getPluginsByDbTypeCode("MYSQL"))
                .thenReturn(java.util.List.of());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> connectionService.testConnection(testRequest));
        
        assertEquals(404, exception.getCode());
        assertTrue(exception.getMessage().contains("No plugin available"));
    }
    
    @Test
    void testConnection_ConnectionFailed() throws Exception {
        // Arrange
        when(pluginManager.getPluginsByDbTypeCode("MYSQL"))
                .thenReturn(java.util.List.of(mockConnectionProvider));
        when(mockConnectionProvider.connect(any(ConnectionConfig.class)))
                .thenThrow(new RuntimeException("Connection refused"));
        
        // Act & Assert - should throw RuntimeException when connection fails
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> connectionService.testConnection(testRequest));
        
        assertTrue(exception.getMessage().contains("Connection refused"));
    }
    
    @Test
    void closeConnection_NotFound() {
        // Act - should return silently (no exception thrown)
        connectionService.closeConnection("non-existent-id");
        
        // Assert - no exception should be thrown, method should return normally
        // (User changed the implementation to return instead of throwing exception)
    }
    
    /**
     * Integration test with real MySQL database.
     * Requires MySQL 8.0 running on localhost:3306.
     * 
     * Database configuration (from docker-compose):
     * - Host: localhost
     * - Port: 3306
     * - Username: root
     * - Password: root
     * - Driver: /Users/dawn/Desktop/mysql-connector-j-8.2.0.jar
     * 
     * To run this test:
     * 1. Start MySQL: docker-compose up -d (in /Users/dawn/docker-compose/mysql)
     * 2. Remove @Disabled annotation or run with: mvn test -Dtest=ConnectionServiceTest#testConnection_RealDatabase
     */
    @Test
    @Tag("integration")
    @Disabled("Requires real MySQL database running")
    void testConnection_RealDatabase() {
        // Arrange - Use real database configuration
        ConnectRequest realRequest = ConnectRequest.builder()
                .dbType("MYSQL")
                .host("localhost")
                .port(3306)
                .database("test")  // Use default test database or create one
                .username("root")
                .password("root")
                .driverJarPath("/Users/dawn/Desktop/mysql-connector-j-8.2.0.jar")
                .timeout(30)
                .build();
        
        // Use real PluginManager (not mock)
        PluginManager realPluginManager = new edu.zsc.ai.plugin.manager.DefaultPluginManager();
        ConnectionServiceImpl realConnectionService = new ConnectionServiceImpl(realPluginManager);
        
        // Act
        ConnectionTestResponse response = realConnectionService.testConnection(realRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(ConnectionTestStatus.SUCCEEDED, response.getStatus());
        assertTrue(response.getDbmsInfo().contains("MySQL"));
        assertTrue(response.getDbmsInfo().contains("8.0"));
        assertTrue(response.getDriverInfo().contains("MySQL Connector/J"));
        assertNotNull(response.getPing());
        assertTrue(response.getPing() >= 0);
        
        System.out.println("Connection Test Result:");
        System.out.println("  Status: " + response.getStatus());
        System.out.println("  DBMS: " + response.getDbmsInfo());
        System.out.println("  Driver: " + response.getDriverInfo());
        System.out.println("  Ping: " + response.getPing() + " ms");
    }
}

