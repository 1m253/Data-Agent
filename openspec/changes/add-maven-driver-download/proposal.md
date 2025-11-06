# Add Maven Driver Download

## Why

Currently, users must manually download JDBC driver JAR files and provide the `driverJarPath` when establishing database connections. This creates friction and requires users to know where to download drivers and manage driver versions manually. When a driver JAR file is missing, the system simply fails with an error, requiring manual intervention.

## What Changes

- Add new `DriverController` with REST API endpoints (all require `databaseType` instead of `pluginId`):
  - `GET /api/drivers/available?databaseType=MySQL` - List available drivers for specific database type (required parameter)
  - `GET /api/drivers/installed?databaseType=MySQL` - List locally installed drivers for specific database type (required parameter)
  - `GET /api/drivers/{databaseType}/versions` - List available versions from Maven Central
  - `POST /api/drivers/download` - Download driver from Maven (user manually triggers download)
  - `DELETE /api/drivers/{databaseType}/{version}` - Delete a locally installed driver
- Create `DriverService` to handle driver download logic (separate from connection management)
- Create driver download components (separated for single responsibility):
  - `MavenUrlBuilder` - Build Maven Central URLs
  - `HttpDownloader` - HTTP download functionality
  - `JarFileValidator` - Validate JAR file integrity
  - `DriverStorageManager` - Manage storage directories and files
  - `MavenDriverDownloader` - Orchestrate download process using above components
  - `MavenMetadataClient` - Query Maven Central for available versions
  - `DriverFileUtil` - Extract version from filename
- Add driver metadata configuration to plugins (Maven coordinates: groupId, artifactId, version)
- Add configuration for local driver storage directory organized by database type (e.g., `drivers/MySQL/`)
- Keep `driverJarPath` as required field (no automatic download)
- Users must manually download drivers via API before using them

## Impact

- **Affected specs**: `driver-management` (NEW), `connection-management` (NO CHANGE - removed auto-download)
- **Affected code**:
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/controller/DriverController.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/service/DriverService.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/service/impl/DriverServiceImpl.java`
  - New: `data-agent-server-app/src/main/java/edu/zsc/ai/util/DriverFileUtil.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/model/MavenCoordinates.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/connection/MavenUrlBuilder.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/connection/HttpDownloader.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/connection/JarFileValidator.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/connection/DriverStorageManager.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/connection/MavenDriverDownloader.java`
  - New: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/connection/MavenMetadataClient.java`
  - Modified: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/Plugin.java` (add getDriverMavenCoordinates method)
  - Modified: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/base/AbstractDatabasePlugin.java` (default implementation)
  - Modified: `data-agent-server-plugins/mysql-plugin/src/main/java/edu/zsc/ai/plugin/mysql/AbstractMysqlPlugin.java` (provide Maven coordinates)
  - Modified: `data-agent-server-plugins/mysql-plugin/src/main/java/edu/zsc/ai/plugin/mysql/Mysql57Plugin.java` (groupId: mysql)
  - Modified: `data-agent-server-plugin/src/main/java/edu/zsc/ai/plugin/manager/PluginManager.java` (add getPlugin method)
  - Modified: `data-agent-server-app/src/main/resources/application.yml` (add driver config)
  - New: Request/Response DTOs for driver APIs (use databaseType instead of pluginId):
    - `DownloadDriverRequest` (databaseType, optional version)
    - `DownloadDriverResponse` (driverPath, databaseType, fileName, version)
    - `AvailableDriverResponse` (pluginId, pluginName, databaseType, defaultVersion, groupId, artifactId, mavenCoordinates)
    - `InstalledDriverResponse` (pluginId, databaseType, fileName, version, filePath, fileSize, lastModified)
    - `DriverVersionResponse` (version, releaseDate, installed)
  - Note: `DriverLoader` remains unchanged (does not handle downloads)
  - Note: `ConnectionServiceImpl` remains unchanged (no automatic download)

## Dependencies

- Requires Maven dependency resolution (can use Maven Resolver API or simple HTTP download from Maven Central)
- Requires file system access for storing downloaded drivers
- Requires plugin metadata to include Maven coordinates for drivers
- Requires access to plugin's DbType to organize drivers by database type

