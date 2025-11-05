package edu.zsc.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zsc.ai.config.PluginConfig;
import edu.zsc.ai.model.dto.request.ConnectRequest;
import edu.zsc.ai.model.dto.response.ConnectionTestResponse;
import edu.zsc.ai.model.enums.ConnectionTestStatus;
import edu.zsc.ai.service.ConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ConnectionController
 *
 * @author Data-Agent
 * @since 0.0.1
 */
@WebMvcTest(ConnectionController.class)
@Import(PluginConfig.class)
class ConnectionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ConnectionService connectionService;
    
    private ConnectRequest testRequest;
    
    @BeforeEach
    void setUp() {
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
    
    @Test
    void testConnection_Success() throws Exception {
        // Arrange
        ConnectionTestResponse testResponse = ConnectionTestResponse.builder()
                .status(ConnectionTestStatus.SUCCEEDED)
                .dbmsInfo("MySQL (ver. 8.0.43)")
                .driverInfo("MySQL Connector/J (ver. mysql-connector-j-8.2.0, JDBC4.2)")
                .ping(14L)
                .build();
        
        when(connectionService.testConnection(any(ConnectRequest.class))).thenReturn(testResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/connections/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data.dbmsInfo").value("MySQL (ver. 8.0.43)"))
                .andExpect(jsonPath("$.data.driverInfo").value("MySQL Connector/J (ver. mysql-connector-j-8.2.0, JDBC4.2)"))
                .andExpect(jsonPath("$.data.ping").value(14));
        
        verify(connectionService).testConnection(any(ConnectRequest.class));
    }
    
    @Test
    void testConnection_ValidationError_MissingDbType() throws Exception {
        // Arrange
        testRequest.setDbType(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/connections/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
        
        verify(connectionService, never()).testConnection(any(ConnectRequest.class));
    }
    
    @Test
    void testConnection_ValidationError_InvalidPort() throws Exception {
        // Arrange
        testRequest.setPort(70000);
        
        // Act & Assert
        mockMvc.perform(post("/api/connections/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
        
        verify(connectionService, never()).testConnection(any(ConnectRequest.class));
    }
    
    @Test
    void closeConnection_Success() throws Exception {
        // Arrange
        String connectionId = "550e8400-e29b-41d4-a716-446655440000";
        doNothing().when(connectionService).closeConnection(connectionId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/connections/{connectionId}", connectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isEmpty());
        
        verify(connectionService).closeConnection(connectionId);
    }
}

