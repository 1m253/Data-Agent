package edu.zsc.ai.config.mcp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * MCP tool provider configuration - aggregates tools from all MCP clients for AI Agent.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class McpToolProviderConfig {

    @Bean("mcpToolProvider")
    public McpToolProvider mcpToolProvider(List<McpClient> mcpClients) {

        if (mcpClients.isEmpty()) {
            log.warn("No MCP clients available, returning empty McpToolProvider");
            return McpToolProvider.builder().build();
        }

        log.info("Creating MCP tool provider from {} clients", mcpClients.size());

        List<McpClient> validClients = new ArrayList<>();
        int clientIndex = 0;
        for (McpClient client : mcpClients) {
            try {
                if (client instanceof DefaultMcpClient defaultClient) {
                    int toolCount = defaultClient.listTools().size();
                    log.info("MCP client #{} has {} tools", clientIndex, toolCount);
                    validClients.add(client);
                }
                clientIndex++;
            } catch (Exception e) {
                log.error("Failed to validate MCP client #{}", clientIndex, e);
                clientIndex++;
            }
        }

        McpToolProvider provider = McpToolProvider.builder()
                .mcpClients(validClients)
                .build();

        log.info("MCP tool provider created successfully with {} clients", validClients.size());
        return provider;
    }
}
