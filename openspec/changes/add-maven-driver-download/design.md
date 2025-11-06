# Design: Maven Driver Download

## Context

Currently, the system requires users to manually download JDBC driver JAR files and provide the `driverJarPath` when establishing connections. This creates friction and requires users to:
1. Know where to download drivers
2. Manage driver versions manually
3. Handle file paths correctly

When a driver JAR is missing, the system fails with a clear error, but requires manual intervention to resolve.

## Goals / Non-Goals

### Goals
- Automatically download missing JDBC drivers from Maven Central
- Cache downloaded drivers locally to avoid re-downloading
- Maintain backward compatibility (existing `driverJarPath` still works)
- Support multiple database types through plugin metadata

### Non-Goals
- Full Maven dependency resolution (only direct driver JAR download)
- Dependency version conflict resolution
- Offline mode support (requires internet for first download)
- Custom Maven repository configuration (use Maven Central only initially)

## Decisions

### Decision: HTTP Download from Maven Central
**What**: Use simple HTTP download from Maven Central repository instead of Maven Resolver API.

**Why**:
- Simpler implementation (no additional dependencies)
- Maven Central has predictable URL structure: `https://repo1.maven.org/maven2/{groupId}/{artifactId}/{version}/{artifactId}-{version}.jar`
- No need for complex dependency resolution
- Faster to implement and maintain

**Alternatives considered**:
- Maven Resolver API: More powerful but adds complexity and dependencies
- Embed Maven: Too heavyweight for this use case

### Decision: Plugin Metadata for Maven Coordinates
**What**: Add method to plugin interface to provide Maven coordinates (groupId, artifactId, version) for drivers.

**Why**:
- Each plugin knows which driver it needs
- Keeps driver information close to plugin implementation
- Allows different plugins to use different driver versions

**Alternatives considered**:
- Configuration file: Less flexible, harder to maintain
- Database mapping: Overkill for static metadata

### Decision: Local Driver Storage Directory Organized by Database Type
**What**: Store downloaded drivers in configurable directory organized by database type (default: `./drivers/{DbType.displayName}/`).

**Why**:
- Allows users to configure storage location
- Default location is application-relative (portable)
- Easy to clean up or share between instances
- Organized by database type makes it easier to manage multiple database drivers

**Directory structure**: `drivers/{DbType.displayName}/{artifactId}-{version}.jar`
- Example: `drivers/MySQL/mysql-connector-j-8.0.33.jar`
- Example: `drivers/PostgreSQL/postgresql-42.7.2.jar`

**Alternatives considered**:
- Flat directory: Less organized, harder to manage multiple database types
- System temp directory: Harder to manage and clean up
- User home directory: May have permission issues

### Decision: Keep `driverJarPath` Required, Auto-Download if File Missing
**What**: Keep `driverJarPath` as required field in connection requests. If the file specified in `driverJarPath` does not exist, automatically download from Maven and update the path.

**Why**:
- Maintains API consistency (field is always required)
- Clear user intent (user specifies where they want the driver)
- Backward compatible (if file exists, use it directly)
- Download happens transparently before driver loading

**Flow logic**:
1. User provides `driverJarPath` (required field)
2. Check if file exists at specified path
3. If file exists → use it directly (existing behavior)
4. If file doesn't exist → query plugin for Maven coordinates → download to organized directory → update `driverJarPath` to downloaded location → use downloaded path

### Decision: Separate Download Logic from DriverLoader
**What**: Keep `DriverLoader` focused only on loading drivers. Download logic handled by separate `MavenDriverDownloader` service, called from connection service layer before `DriverLoader`.

**Why**:
- Single Responsibility Principle: DriverLoader loads, Downloader downloads
- Easier to test and maintain
- Clear separation of concerns
- Download logic can be reused elsewhere if needed

**Flow**:
1. Connection service receives request with `driverJarPath`
2. Connection service checks if file exists
3. If missing, connection service calls `MavenDriverDownloader.download()` with plugin metadata
4. `MavenDriverDownloader` downloads and returns actual file path
5. Connection service updates `driverJarPath` in config
6. Connection service calls `DriverLoader.loadDriver()` with updated path

## Risks / Trade-offs

### Risk: Network Dependency
**Risk**: First connection requires internet access to download driver.

**Mitigation**: 
- Cache downloaded drivers locally
- Provide clear error message if download fails
- Allow users to pre-download drivers or provide `driverJarPath` manually

### Risk: Driver Version Conflicts
**Risk**: Different plugins might need different driver versions.

**Mitigation**:
- Store drivers with version in filename
- Each plugin specifies its required version
- No conflict resolution needed (each plugin uses its own driver)

### Risk: Maven Central Availability
**Risk**: Maven Central might be unavailable or slow.

**Mitigation**:
- Add retry logic with exponential backoff
- Provide clear error messages
- Allow fallback to manual `driverJarPath`

### Trade-off: Simplicity vs Features
**Trade-off**: Simple HTTP download vs full Maven dependency resolution.

**Choice**: Start simple. Can add Maven Resolver API later if needed.

## Migration Plan

### Phase 1: Implementation
1. Add `MavenDriverDownloader` service (separate from DriverLoader)
2. Add download logic in connection service layer
3. Add plugin metadata for Maven coordinates
4. Keep `driverJarPath` required in DTOs (no changes needed)

### Phase 2: Testing
1. Unit tests for downloader
2. Integration tests for auto-download flow
3. Backward compatibility tests

### Phase 3: Deployment
1. Deploy with feature flag (if needed)
2. Monitor download success rates
3. Collect feedback from users

### Rollback
- Feature can be disabled by requiring `driverJarPath` in validation
- No data migration needed (drivers are just files)

## Open Questions

1. Should we support custom Maven repositories? (Future enhancement)
2. Should we verify JAR file signatures? (Security consideration)
3. Should we support proxy configuration for downloads? (Enterprise environments)
4. Should we add driver version management UI? (Future enhancement)

