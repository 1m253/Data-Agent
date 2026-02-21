package edu.zsc.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationParameters;
import edu.zsc.ai.common.constant.RequestContextConstant;
import edu.zsc.ai.common.constant.ToolMessageConstants;
import edu.zsc.ai.domain.model.dto.response.db.ConnectionResponse;
import edu.zsc.ai.domain.service.db.DbConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConnectionTool {

    private final DbConnectionService dbConnectionService;

    @Tool({
        "List all database connections owned by the current user.",
        "Use when the user asks for their connections, wants to switch connection, or needs to see available connections."
    })
    public String getMyConnections(InvocationParameters parameters) {
        log.info("{} getMyConnections", ToolMessageConstants.TOOL_LOG_PREFIX_BEFORE);
        try {
            Long userId = parameters.get(RequestContextConstant.USER_ID);
            if (userId == null) {
                return ToolMessageConstants.USER_CONTEXT_MISSING;
            }
            List<ConnectionResponse> connections = dbConnectionService.getAllConnections(userId);
            if (connections == null || connections.isEmpty()) {
                log.info("{} getMyConnections -> {}", ToolMessageConstants.TOOL_LOG_PREFIX_DONE,
                        ToolMessageConstants.EMPTY_NO_CONNECTIONS);
                return ToolMessageConstants.EMPTY_NO_CONNECTIONS;
            }
            log.info("{} getMyConnections, result size={}", ToolMessageConstants.TOOL_LOG_PREFIX_DONE, connections.size());
            return edu.zsc.ai.util.JsonUtil.object2json(connections);
        } catch (Exception e) {
            log.error("{} getMyConnections", ToolMessageConstants.TOOL_LOG_PREFIX_ERROR, e);
            return e.getMessage();
        }
    }

    @Tool({
        "Get details of a specific database connection by its connectionId.",
        "Use when you need full connection info (host, port, database name, etc.) for a given connection id."
    })
    public String getConnectionById(
            @P("The connection id (from session context or getMyConnections result)") Long connectionId,
            InvocationParameters parameters) {
        log.info("{} getConnectionById, connectionId={}", ToolMessageConstants.TOOL_LOG_PREFIX_BEFORE, connectionId);
        try {
            Long userId = parameters.get(RequestContextConstant.USER_ID);
            if (userId == null) {
                return ToolMessageConstants.USER_CONTEXT_MISSING;
            }
            ConnectionResponse connection = dbConnectionService.getConnectionById(connectionId, userId);
            log.info("{} getConnectionById, connectionId={}", ToolMessageConstants.TOOL_LOG_PREFIX_DONE, connectionId);
            return edu.zsc.ai.util.JsonUtil.object2json(connection);
        } catch (Exception e) {
            log.error("{} getConnectionById, connectionId={}", ToolMessageConstants.TOOL_LOG_PREFIX_ERROR, connectionId, e);
            return e.getMessage();
        }
    }
}
