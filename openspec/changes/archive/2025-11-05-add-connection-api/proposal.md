# Add Connection Management API

## Why

Currently, there is no REST API to expose database connection capabilities provided by the plugin system. Users need a way to test database connections, establish connections, and manage connection lifecycle through HTTP endpoints.

## What Changes

- Add `ConnectionController` with REST endpoints for connection management
- Create Request DTOs: `TestConnectionRequest`, `ConnectRequest`, `CloseConnectionRequest`
- Create Response DTOs: `ConnectionTestResponse`, `ConnectionResponse`
- Implement `ConnectionService` interface and `ConnectionServiceImpl` for business logic
- Follow all Java design guidelines (five-piece set pattern, no magic values, unified response format)
- Integrate with existing plugin-management capability (use PluginManager to find connection providers)

## Impact

- **Affected specs**: New capability `connection-management`
- **Affected code**:
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/controller/ConnectionController.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/service/ConnectionService.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/service/impl/ConnectionServiceImpl.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/model/dto/request/TestConnectionRequest.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/model/dto/request/ConnectRequest.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/model/dto/response/ConnectionTestResponse.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/model/dto/response/ConnectionResponse.java`
  - Dependencies: Uses `PluginManager` from plugin-management capability
  - Dependencies: Uses `ConnectionProvider` interface from plugin API

## Dependencies

- Requires `plugin-management` spec to be implemented
- Requires `ConnectionProvider` capability in plugins

