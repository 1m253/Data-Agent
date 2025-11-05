# Implementation Tasks

## 1. Create Request DTOs
- [x] 1.1 Create `ConnectRequest` DTO (dbType, host, port, database, username, password, driverJarPath, timeout, properties)
- [x] 1.2 Add Jakarta validation annotations to all request fields
- [x] 1.3 Simplified: Use single DTO for both test and connect operations

## 2. Create Response DTOs
- [x] 2.1 Create `ConnectionTestResponse` DTO with status (enum), dbmsInfo, driverInfo, ping
- [x] 2.2 Create `ConnectionTestStatus` enum (SUCCEEDED, FAILED)
- [x] 2.3 testConnection returns ApiResponse<ConnectionTestResponse> with detailed information
- [x] 2.4 closeConnection returns ApiResponse<Void>

## 3. Implement Service Layer
- [x] 3.1 Create `ConnectionService` interface with methods (testConnection, closeConnection)
- [x] 3.2 Create `ConnectionServiceImpl` with business logic
- [x] 3.3 Inject `PluginManager` into service implementation
- [x] 3.4 Implement connection lifecycle management (storing active connections)
- [x] 3.5 Implement proper error handling with RuntimeException
- [x] 3.6 Implement plugin selection by dbType code (using getPluginsByDbTypeCode)
- [x] 3.7 Simplified: Direct plugin selection without enum conversion
- [x] 3.8 Implement detailed connection test with DatabaseMetaData (DBMS version, driver info, ping)

## 4. Implement Controller Layer
- [x] 4.1 Create `ConnectionController` with @RestController and @RequestMapping("/api/connections")
- [x] 4.2 Implement POST `/test` endpoint for testing connections (returns detailed info)
- [x] 4.3 Implement DELETE `/{connectionId}` endpoint for closing connections
- [x] 4.4 Add proper validation using @Valid annotation
- [x] 4.5 Return ApiResponse<T> for all endpoints
- [x] 4.6 Note: POST `/connect` endpoint removed - connections are created internally by other features

## 5. Testing
- [x] 5.1 Create unit tests for ConnectionServiceImpl (5 tests: success, no plugin, connection failed, close not found)
- [x] 5.2 Create integration tests for ConnectionController endpoints (4 tests: success, validation errors)
- [x] 5.3 Test with MySQL plugin (automatic selection by dbType code)
- [x] 5.4 Test error scenarios (no plugin, connection failures)
- [x] 5.5 Add integration test with real MySQL database (disabled by default)

## 6. Configuration
- [x] 6.1 Create PluginConfig to expose PluginManager as Spring Bean
- [x] 6.2 Add JavaDoc comments to all classes and methods
- [x] 6.3 Fix all linter warnings
- [x] 6.4 Add getPluginsByDbTypeCode method to PluginManager interface
- [x] 6.5 Remove PluginException dependencies (use RuntimeException instead)

