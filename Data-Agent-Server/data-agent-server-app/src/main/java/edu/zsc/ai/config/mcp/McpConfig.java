package edu.zsc.ai.config.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MCP hybrid configuration
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mcp.enabled", havingValue = "true", matchIfMissing = true)
public class McpConfig {
    
    private final McpProperties properties;
    
    @Bean
    public List<McpClient> mcpClients() {
        if (properties.getServers().isEmpty()) {
            return List.of();
        }
        
        boolean isWindows = isWindows();
        
        List<McpClient> clients = properties.getServers().entrySet().stream()
            .filter(e -> e.getValue().isEnabled())
            .map(e -> createClient(e.getKey(), e.getValue(), isWindows))
            .filter(Objects::nonNull)
            .toList();
        
        log.info("Initialized {} MCP client(s)", clients.size());
        return clients;
    }
    
    private McpClient createClient(String name, McpProperties.ServerConfig config, boolean isWindows) {
        try {
            config.validate();
            List<String> command = buildCommand(config, isWindows);
            
            McpTransport transport = StdioMcpTransport.builder()
                .command(command)
                .environment(config.getEnv())
                .build();
            
            McpClient client = DefaultMcpClient.builder()
                .transport(transport)
                .key(name)
                .build();
            
            return client;
            
        } catch (Exception e) {
            log.error("Failed to create MCP client '{}': {}", name, e.getMessage());
            return null;
        }
    }
    
    private List<String> buildCommand(McpProperties.ServerConfig config, boolean isWindows) {
        List<String> command = new ArrayList<>();
        
        if (config.getPackageName() != null) {
            command.add(getNpxCommand(isWindows));
            command.add(config.getPackageName());
            return command;
        }
        
        if (config.getCommand() != null) {
            command.add(adaptCommand(config.getCommand(), isWindows));
            if (config.getArgs() != null && !config.getArgs().isEmpty()) {
                command.addAll(config.getArgs());
            }
            return command;
        }
        
        throw new IllegalArgumentException(
            "Either 'packageName' or 'command' must be specified");
    }
    
    private String getNpxCommand(boolean isWindows) {
        return isWindows ? "npx.cmd" : "npx";
    }
    
    private String adaptCommand(String cmd, boolean isWindows) {
        if (isWindows && !cmd.endsWith(".cmd") && !cmd.endsWith(".exe")) {
            if (cmd.equals("npx") || cmd.equals("npm")) {
                return cmd + ".cmd";
            }
        }
        return cmd;
    }
    
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
