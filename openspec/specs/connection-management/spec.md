# connection-management Specification

## Purpose
TBD - created by archiving change add-connection-api. Update Purpose after archive.
## Requirements
### Requirement: Test Database Connection

The system SHALL provide an API endpoint to test database connection without establishing a persistent connection.

#### Scenario: Test connection successfully
- **WHEN** POST /api/connections/test is called with valid connection parameters
- **THEN** the system SHALL use PluginManager to find the plugin by pluginId
- **AND** verify the plugin implements ConnectionProvider capability
- **AND** call ConnectionProvider.testConnection() with the provided configuration
- **AND** return success response with connection test result and response time
- **AND** NOT store any persistent connection

#### Scenario: Test connection with invalid plugin ID
- **WHEN** POST /api/connections/test is called with non-existent pluginId
- **THEN** the system SHALL return 404 error with message "Plugin not found: {pluginId}"
- **AND** NOT attempt to establish connection

#### Scenario: Test connection with plugin not supporting CONNECTION capability
- **WHEN** POST /api/connections/test is called with pluginId that doesn't implement ConnectionProvider
- **THEN** the system SHALL return 400 error with message "Plugin {pluginId} does not support CONNECTION capability"

#### Scenario: Test connection failure
- **WHEN** POST /api/connections/test is called but connection fails (invalid credentials, network error, etc.)
- **THEN** the system SHALL return 500 error with detailed failure message from plugin
- **AND** include connection parameters (host, port, database) in error context for debugging

#### Scenario: Test connection with validation errors
- **WHEN** POST /api/connections/test is called with invalid parameters (empty host, invalid port, etc.)
- **THEN** the system SHALL return 400 error with validation failure details
- **AND** NOT attempt to connect to database

### Requirement: Establish Database Connection

The system SHALL provide an API endpoint to establish and maintain a persistent database connection.

#### Scenario: Establish connection successfully
- **WHEN** POST /api/connections/connect is called with valid connection parameters
- **THEN** the system SHALL find the plugin by pluginId using PluginManager
- **AND** call ConnectionProvider.connect() to establish connection
- **AND** generate a unique connectionId (UUID format)
- **AND** store the connection in active connections registry
- **AND** return connection information including connectionId, pluginId, and connection details

#### Scenario: Establish connection with non-existent plugin
- **WHEN** POST /api/connections/connect is called with pluginId that doesn't exist
- **THEN** the system SHALL return 404 error with message "Plugin not found: {pluginId}"
- **AND** NOT store any connection

#### Scenario: Establish connection failure
- **WHEN** POST /api/connections/connect is called but connection fails
- **THEN** the system SHALL return 500 error with failure reason
- **AND** NOT store any connection in registry

#### Scenario: Multiple connections to same database
- **WHEN** POST /api/connections/connect is called multiple times with same connection parameters
- **THEN** the system SHALL create separate connections each time
- **AND** assign different connectionId to each connection
- **AND** store all connections independently in registry

### Requirement: Close Database Connection

The system SHALL provide an API endpoint to close an active database connection and release resources.

#### Scenario: Close connection successfully
- **WHEN** DELETE /api/connections/{connectionId} is called with valid connectionId
- **THEN** the system SHALL retrieve the connection from active connections registry
- **AND** call ConnectionProvider.closeConnection() to release resources
- **AND** remove the connection from active connections registry
- **AND** return success response

#### Scenario: Close non-existent connection
- **WHEN** DELETE /api/connections/{connectionId} is called with connectionId that doesn't exist
- **THEN** the system SHALL return 404 error with message "Connection not found: {connectionId}"

#### Scenario: Close already closed connection
- **WHEN** DELETE /api/connections/{connectionId} is called for a connection that was already closed
- **THEN** the system SHALL return 404 error with message "Connection not found: {connectionId}"

#### Scenario: Close connection with cleanup failure
- **WHEN** DELETE /api/connections/{connectionId} is called but Connection.close() throws exception
- **THEN** the system SHALL log the error
- **AND** remove the connection from registry anyway
- **AND** return 500 error with cleanup failure message

### Requirement: Connection Parameter Validation

The system SHALL validate all connection parameters before attempting to establish connections.

#### Scenario: Validate required fields
- **WHEN** any connection endpoint is called with missing required fields (pluginId, host, port, driverJarPath)
- **THEN** the system SHALL return 400 error with validation message indicating missing field
- **AND** NOT attempt to connect to database

#### Scenario: Validate port range
- **WHEN** any connection endpoint is called with port < 1 or port > 65535
- **THEN** the system SHALL return 400 error with message "Port must be between 1 and 65535"

#### Scenario: Validate timeout range
- **WHEN** any connection endpoint is called with timeout < 1 or timeout > 300
- **THEN** the system SHALL return 400 error with message "Timeout must be between 1 and 300 seconds"

### Requirement: Connection Lifecycle Management

The system SHALL manage the lifecycle of all active database connections and ensure proper cleanup.

#### Scenario: Track active connections
- **WHEN** connections are established via POST /api/connections/connect
- **THEN** the system SHALL store Connection objects in thread-safe registry (ConcurrentHashMap)
- **AND** associate each connection with unique connectionId
- **AND** track connection metadata (pluginId, host, port, database, username)

#### Scenario: Application shutdown cleanup
- **WHEN** the application is shutting down
- **THEN** the system SHALL close all active connections in registry
- **AND** log any errors during cleanup without throwing exceptions
- **AND** clear the active connections registry

#### Scenario: Thread-safe connection operations
- **WHEN** multiple concurrent requests attempt to connect/close connections
- **THEN** all operations SHALL be thread-safe
- **AND** no race conditions SHALL occur in connection registry access

### Requirement: Plugin Integration

The system SHALL integrate with the plugin-management system to discover and use database plugins.

#### Scenario: Lookup plugin by ID
- **WHEN** connection endpoint receives pluginId parameter
- **THEN** the system SHALL use PluginManager.getPlugin(pluginId) to find plugin
- **AND** check if plugin is in STARTED state
- **AND** return error if plugin state is not STARTED

#### Scenario: Verify plugin capability
- **WHEN** plugin is found by ID
- **THEN** the system SHALL verify plugin implements ConnectionProvider interface
- **AND** cast plugin to ConnectionProvider safely
- **AND** return error if plugin doesn't support CONNECTION capability

#### Scenario: Plugin not started
- **WHEN** plugin exists but is not in STARTED state (e.g., FAILED, INITIALIZED)
- **THEN** the system SHALL return 503 error with message "Plugin {pluginId} is not available. Current state: {state}"

### Requirement: Error Handling and Messages

The system SHALL provide clear error messages for all failure scenarios.

#### Scenario: Return detailed validation errors
- **WHEN** request validation fails
- **THEN** the system SHALL return 400 error
- **AND** include field name and validation constraint in error message
- **AND** use Jakarta Bean Validation messages

#### Scenario: Return plugin errors
- **WHEN** plugin operation fails (testConnection, connect, closeConnection)
- **THEN** the system SHALL catch PluginException
- **AND** extract error message from exception
- **AND** return 500 error with plugin error message
- **AND** log full stack trace for debugging

#### Scenario: Return business logic errors
- **WHEN** business validation fails (plugin not found, connection not found, etc.)
- **THEN** the system SHALL throw BusinessException with appropriate error code
- **AND** GlobalExceptionHandler SHALL convert to ApiResponse format
- **AND** return appropriate HTTP status code

### Requirement: Unified Response Format

The system SHALL return all responses in unified ApiResponse format.

#### Scenario: Success response with data
- **WHEN** any endpoint succeeds
- **THEN** the system SHALL return ApiResponse with code=200, message="success", and data object
- **AND** data object SHALL contain endpoint-specific response DTO

#### Scenario: Success response without data
- **WHEN** DELETE endpoint succeeds
- **THEN** the system SHALL return ApiResponse with code=200, message="success", and data=null

#### Scenario: Error response
- **WHEN** any endpoint fails
- **THEN** the system SHALL return ApiResponse with appropriate error code, error message, and data=null
- **AND** error codes SHALL follow standard mapping (400=validation, 404=not found, 500=internal error, 503=service unavailable)

### Requirement: Request DTO Design

The system SHALL define Request DTOs following Java design guidelines.

#### Scenario: TestConnectionRequest structure
- **WHEN** TestConnectionRequest is created
- **THEN** it SHALL include fields: pluginId, host, port, database, username, password, driverJarPath, timeout, properties
- **AND** all fields SHALL have appropriate validation annotations
- **AND** class SHALL use @Data annotation

#### Scenario: ConnectRequest structure
- **WHEN** ConnectRequest is created
- **THEN** it SHALL include same fields as TestConnectionRequest
- **AND** follow same validation rules

### Requirement: Response DTO Design

The system SHALL define Response DTOs following Java design guidelines.

#### Scenario: ConnectionTestResponse structure
- **WHEN** ConnectionTestResponse is created
- **THEN** it SHALL include fields: success (boolean), message (String), responseTime (Long milliseconds)
- **AND** class SHALL use @Data annotation
- **AND** NOT include sensitive information (passwords)

#### Scenario: ConnectionResponse structure
- **WHEN** ConnectionResponse is created
- **THEN** it SHALL include fields: connectionId, pluginId, host, port, database, username, connected (boolean)
- **AND** NOT include password field
- **AND** class SHALL use @Data annotation

