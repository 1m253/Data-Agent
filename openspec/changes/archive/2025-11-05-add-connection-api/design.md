# Connection Management API Design

## Context

The Data-Agent application uses a plugin-based architecture to support multiple database types. Each plugin can provide CONNECTION capability through the `ConnectionProvider` interface. This design document outlines the REST API layer that exposes connection management functionality to clients.

## Goals / Non-Goals

### Goals
- Provide REST API endpoints for testing, establishing, and closing database connections
- Follow all Java design guidelines strictly (five-piece set, no magic values, unified responses)
- Support all database plugins that implement ConnectionProvider capability
- Manage connection lifecycle (track active connections, cleanup on close)
- Provide detailed error messages for connection failures
- Support connection parameters validation

### Non-Goals
- Connection pooling (defer to future)
- Persistent connection storage (connections are in-memory only)
- Connection sharing between users (defer to future when auth is added)
- Query execution endpoints (separate capability)
- Schema/metadata endpoints (separate capability)

## Decisions

### Decision 1: Connection Lifecycle Management

**Decision**: Store active connections in-memory using a `ConcurrentHashMap<String, Connection>` with connectionId as key.

**Rationale**:
- Simple and fast for MVP
- Thread-safe concurrent access
- Easy cleanup on application shutdown
- No external dependency needed

**Alternatives considered**:
- Connection pooling (HikariCP, Druid): Too complex for initial implementation
- Database-backed storage: Unnecessary overhead, connections are ephemeral
- Redis/external cache: Adds infrastructure complexity

**Trade-offs**:
- Connections lost on application restart (acceptable for MVP)
- Memory overhead for large number of connections (can be addressed with limits later)

### Decision 2: Connection ID Generation

**Decision**: Use UUID for connection identifiers.

**Rationale**:
- Globally unique, no collision risk
- No need for centralized ID generation
- Standard Java library support

**Alternatives considered**:
- Sequential ID: Risk of collision, not thread-safe without synchronization
- Custom format (timestamp + random): More complex, no benefit

### Decision 3: Service Layer Dependencies

**Decision**: Inject `PluginManager` directly into `ConnectionServiceImpl` to discover plugins by ID.

**Rationale**:
- Direct access to plugin lookup functionality
- No need for intermediate layer
- PluginManager already provides required methods (getPlugin, getPluginsByDbType)

**Alternatives considered**:
- Create separate PluginService: Unnecessary abstraction layer
- Direct SPI lookup: Bypasses PluginManager capabilities (state tracking, lifecycle)

### Decision 4: Error Handling Strategy

**Decision**: Use `BusinessException` with specific error codes for different failure scenarios.

**Error Code Mapping**:
- 400: Invalid request parameters (validation errors)
- 404: Plugin not found, connection not found
- 500: Connection failure, internal errors
- 503: Plugin not available (not started or failed state)

**Rationale**:
- Consistent with existing exception handling pattern
- GlobalExceptionHandler already configured
- Clear error codes for client-side handling

### Decision 5: Request/Response DTO Structure

**Decision**: Create separate DTOs for each operation rather than reusing ConnectionConfig.

**Rationale**:
- Clear API contract for each endpoint
- Allows adding endpoint-specific fields without affecting plugin API
- Better validation control (e.g., different validation rules for test vs connect)
- Decouples controller layer from plugin internal models

**DTOs**:
- `TestConnectionRequest`: All connection parameters + pluginId
- `ConnectRequest`: Same as TestConnectionRequest
- `ConnectionTestResponse`: success, message, responseTime
- `ConnectionResponse`: connectionId, pluginId, database info, connection state

## Technical Details

### API Endpoints

#### POST /api/connections/test
Test database connection without establishing persistent connection.

**Request Body**:
```json
{
  "pluginId": "mysql-8",
  "host": "localhost",
  "port": 3306,
  "database": "testdb",
  "username": "root",
  "password": "password",
  "driverJarPath": "/path/to/mysql-connector.jar",
  "timeout": 10,
  "properties": {
    "useSSL": "false"
  }
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "message": "Connection test successful",
    "responseTime": 234
  }
}
```

#### POST /api/connections/connect
Establish a persistent database connection.

**Request Body**: Same as test endpoint

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "connectionId": "550e8400-e29b-41d4-a716-446655440000",
    "pluginId": "mysql-8",
    "host": "localhost",
    "port": 3306,
    "database": "testdb",
    "username": "root",
    "connected": true
  }
}
```

#### DELETE /api/connections/{connectionId}
Close an active connection and release resources.

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### Service Layer Design

```java
public interface ConnectionService {
    ConnectionTestResponse testConnection(TestConnectionRequest request);
    ConnectionResponse connect(ConnectRequest request);
    void closeConnection(String connectionId);
}
```

**Key Implementation Details**:
- Convert Request DTO → ConnectionConfig before calling plugin
- Use PluginManager.getPlugin(pluginId) to find plugin
- Cast plugin to ConnectionProvider (check capability first)
- Generate UUID for connectionId
- Store Connection in ConcurrentHashMap
- Handle plugin exceptions and convert to BusinessException

### Validation Rules

All request DTOs must validate:
- `pluginId`: @NotBlank
- `host`: @NotBlank
- `port`: @Min(1), @Max(65535)
- `username`: @NotBlank
- `password`: No validation (can be empty for some databases)
- `driverJarPath`: @NotBlank
- `timeout`: @Min(1), @Max(300)
- `database`: Optional (some databases don't require it)
- `properties`: Optional

### Thread Safety

- `ConcurrentHashMap` for connection storage
- No synchronization needed for connection lookup (read-heavy)
- Connection close operation must be thread-safe (remove from map + close connection)

### Resource Cleanup

- Implement `@PreDestroy` method in ConnectionServiceImpl
- Close all active connections on application shutdown
- Log any errors during cleanup (don't throw)

## Risks / Trade-offs

### Risk 1: Memory Leak from Unclosed Connections
**Mitigation**: 
- Document requirement to close connections
- Add connection timeout/idle detection (future enhancement)
- Implement cleanup on application shutdown

### Risk 2: Concurrent Access to Same Connection
**Mitigation**:
- Document that connections should not be shared
- Consider adding connection state tracking (in-use flag) in future

### Risk 3: Plugin Not Available
**Mitigation**:
- Check plugin state before using (STARTED state required)
- Return clear error message with plugin state
- Suggest plugin loading/initialization if needed

## Migration Plan

N/A - This is a new feature with no existing functionality to migrate.

## Open Questions

1. **Q**: Should we limit the number of concurrent connections per user/globally?
   **A**: Defer to future. Start without limits, add if needed based on usage.

2. **Q**: Should we provide an endpoint to list all active connections?
   **A**: Defer to future. Start with basic CRUD, add monitoring endpoints later.

3. **Q**: Should connection credentials be encrypted in request/response?
   **A**: Use HTTPS for transport security. In-memory encryption deferred to future when adding persistence.

4. **Q**: Should we support connection keep-alive/heartbeat?
   **A**: Defer to future. Connections are short-lived for now.

