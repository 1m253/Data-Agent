# driver-management Specification

## Purpose
TBD - created by archiving change add-maven-driver-download. Update Purpose after archive.

## Requirements

### Requirement: Download Driver from Maven

The system SHALL provide an API endpoint to download JDBC drivers from Maven Central repository. The download process consists of multiple steps.

#### Scenario: Download driver process steps
- **WHEN** POST /api/drivers/download is called with pluginId and version
- **THEN** the download process SHALL follow these steps:
  1. Find plugin by pluginId using PluginManager
  2. Verify plugin provides Maven coordinates (groupId, artifactId)
  3. Check if driver already exists in database-type-specific directory (skip download if exists)
  4. Construct Maven Central download URL: `https://repo1.maven.org/maven2/{groupId}/{artifactId}/{version}/{artifactId}-{version}.jar`
  5. Send HTTP GET request to download JAR file
  6. Validate downloaded file (verify it's a valid JAR file, not empty)
  7. Determine database type from plugin metadata (DbType.displayName)
  8. Create database-type-specific directory if not exists (e.g., `drivers/MySQL/`)
  9. Save JAR file with filename format: `{artifactId}-{version}.jar`
  10. Return success response with downloaded driver path

#### Scenario: Download driver successfully with default version
- **WHEN** POST /api/drivers/download is called with pluginId (no version specified)
- **THEN** the system SHALL find the plugin by pluginId using PluginManager
- **AND** verify the plugin provides Maven coordinates for driver
- **AND** use default version from plugin metadata
- **AND** follow the download process steps
- **AND** return success response with downloaded driver path

#### Scenario: Download driver with specified version
- **WHEN** POST /api/drivers/download is called with pluginId and version
- **THEN** the system SHALL find the plugin by pluginId using PluginManager
- **AND** verify the plugin provides Maven coordinates (groupId, artifactId)
- **AND** use specified version instead of default version
- **AND** follow the download process steps
- **AND** return success response with downloaded driver path

#### Scenario: Use cached driver if already downloaded
- **WHEN** POST /api/drivers/download is called with pluginId
- **AND** driver JAR already exists in database-type-specific directory (same version)
- **THEN** the system SHALL skip download steps (steps 4-9)
- **AND** return success response with existing driver path
- **AND** NOT attempt to download again

#### Scenario: Download driver with non-existent plugin
- **WHEN** POST /api/drivers/download is called with non-existent pluginId
- **THEN** the system SHALL return 404 error with message "Plugin not found: {pluginId}"

#### Scenario: Download driver with plugin without Maven coordinates
- **WHEN** POST /api/drivers/download is called with pluginId that doesn't provide Maven coordinates
- **THEN** the system SHALL return 400 error with message "Plugin {pluginId} does not provide Maven coordinates for driver download"

#### Scenario: Download failure at HTTP request step
- **WHEN** POST /api/drivers/download is called but HTTP request fails (network error, timeout, etc.)
- **THEN** the system SHALL return 500 error with message indicating download failure
- **AND** include error details (network error, timeout, etc.)
- **AND** NOT save any file

#### Scenario: Download failure at validation step
- **WHEN** POST /api/drivers/download is called but downloaded file validation fails (invalid JAR, empty file, etc.)
- **THEN** the system SHALL delete the invalid file
- **AND** return 500 error with message "Downloaded file is invalid or corrupted"
- **AND** include validation error details

#### Scenario: Download with invalid version
- **WHEN** POST /api/drivers/download is called with invalid version (non-existent version)
- **THEN** the system SHALL return 400 error with message "Driver version {version} not found for plugin {pluginId}"

### Requirement: List Available Drivers

The system SHALL provide an API endpoint to list all available drivers that can be downloaded, based on registered plugins.

#### Scenario: List all available drivers
- **WHEN** GET /api/drivers/available is called
- **THEN** the system SHALL query PluginManager for all plugins that provide Maven coordinates
- **AND** return list of available drivers with metadata (pluginId, pluginName, databaseType, defaultVersion, groupId, artifactId)
- **AND** return empty list if no plugins provide Maven coordinates

#### Scenario: List available drivers with plugin details
- **WHEN** GET /api/drivers/available is called
- **THEN** the system SHALL return driver information including:
  - pluginId (e.g., "mysql-8")
  - pluginName (e.g., "MySQL 8.0+")
  - databaseType (e.g., "MySQL")
  - defaultVersion (e.g., "8.0.33")
  - groupId (e.g., "com.mysql")
  - artifactId (e.g., "mysql-connector-j")
  - mavenCoordinates (full coordinates string)

### Requirement: List Installed Drivers

The system SHALL provide an API endpoint to list all drivers that have been downloaded and stored locally.

#### Scenario: List all installed drivers
- **WHEN** GET /api/drivers/installed is called
- **THEN** the system SHALL scan the driver storage directory (organized by database type)
- **AND** return list of installed drivers with metadata (pluginId, databaseType, fileName, version, filePath, fileSize, downloadDate)
- **AND** return empty list if no drivers are installed

#### Scenario: List installed drivers by database type
- **WHEN** GET /api/drivers/installed?databaseType=MySQL is called
- **THEN** the system SHALL scan only the MySQL driver directory
- **AND** return list of installed MySQL drivers
- **AND** filter results by databaseType parameter

#### Scenario: List installed drivers with file details
- **WHEN** GET /api/drivers/installed is called
- **THEN** the system SHALL return driver information including:
  - pluginId (inferred from directory structure or metadata)
  - databaseType (from directory name, e.g., "MySQL")
  - fileName (e.g., "mysql-connector-j-8.0.33.jar")
  - version (extracted from filename)
  - filePath (full path to JAR file)
  - fileSize (in bytes)
  - lastModified (file modification timestamp)

### Requirement: List Driver Versions from Maven Central

The system SHALL provide an API endpoint to list available versions for a specific driver from Maven Central (remote repository).

#### Scenario: List available versions for driver from Maven Central
- **WHEN** GET /api/drivers/{pluginId}/versions is called
- **THEN** the system SHALL find the plugin by pluginId using PluginManager
- **AND** verify the plugin provides Maven coordinates (groupId, artifactId)
- **AND** query Maven Central metadata API for available versions
- **AND** return list of available versions sorted by version number (newest first)
- **AND** include version information (version string, release date if available)
- **AND** indicate which versions are already installed locally (if any)

#### Scenario: List versions with non-existent plugin
- **WHEN** GET /api/drivers/{pluginId}/versions is called with non-existent pluginId
- **THEN** the system SHALL return 404 error with message "Plugin not found: {pluginId}"

#### Scenario: List versions with plugin without Maven coordinates
- **WHEN** GET /api/drivers/{pluginId}/versions is called with pluginId that doesn't provide Maven coordinates
- **THEN** the system SHALL return 400 error with message "Plugin {pluginId} does not provide Maven coordinates"

#### Scenario: Query Maven Central failure
- **WHEN** GET /api/drivers/{pluginId}/versions is called but Maven Central query fails
- **THEN** the system SHALL return 500 error with message indicating query failure
- **AND** suggest using default version from plugin metadata
- **AND** optionally return locally installed versions if available

### Requirement: Delete Installed Driver

The system SHALL provide an API endpoint to delete a locally installed driver.

#### Scenario: Delete driver successfully
- **WHEN** DELETE /api/drivers/{pluginId}/{version} is called
- **THEN** the system SHALL find the plugin by pluginId using PluginManager
- **AND** locate the driver file in database-type-specific directory
- **AND** verify the file exists
- **AND** delete the driver JAR file from local storage
- **AND** return success response

#### Scenario: Delete driver with file path
- **WHEN** DELETE /api/drivers is called with filePath in request body
- **THEN** the system SHALL verify the file path is within driver storage directory (security check)
- **AND** verify the file exists
- **AND** delete the driver JAR file
- **AND** return success response

#### Scenario: Delete non-existent driver
- **WHEN** DELETE /api/drivers/{pluginId}/{version} is called with non-existent driver
- **THEN** the system SHALL return 404 error with message "Driver not found: {pluginId}/{version}"

#### Scenario: Delete driver with non-existent plugin
- **WHEN** DELETE /api/drivers/{pluginId}/{version} is called with non-existent pluginId
- **THEN** the system SHALL return 404 error with message "Plugin not found: {pluginId}"

#### Scenario: Delete driver file in use
- **WHEN** DELETE /api/drivers/{pluginId}/{version} is called
- **AND** driver file is currently being used by an active connection
- **THEN** the system SHALL return 409 error with message "Driver is currently in use and cannot be deleted"
- **AND** NOT delete the file

#### Scenario: Delete driver outside storage directory
- **WHEN** DELETE /api/drivers is called with filePath outside driver storage directory
- **THEN** the system SHALL return 400 error with message "Invalid driver path: path must be within driver storage directory"
- **AND** NOT delete any file

The system SHALL manage local storage of downloaded driver JAR files organized by database type.

#### Scenario: Store drivers in database-type-specific directory
- **WHEN** driver is downloaded from Maven
- **THEN** the system SHALL determine database type from plugin metadata (DbType.displayName)
- **AND** save JAR file to directory: {storageDir}/{DbType.displayName}/ (default: ./drivers/{DbType.displayName}/)
- **AND** use filename format: {artifactId}-{version}.jar (e.g., mysql-connector-j-8.0.33.jar)
- **AND** create database-type-specific directory if it does not exist
- **AND** create parent storage directory if it does not exist

#### Scenario: Validate downloaded JAR file
- **WHEN** driver JAR is downloaded from Maven
- **THEN** the system SHALL verify the downloaded file is a valid JAR file
- **AND** verify file is not empty
- **AND** if validation fails, delete invalid file and return error

#### Scenario: Directory organization example
- **WHEN** MySQL driver is downloaded
- **THEN** the system SHALL save to `drivers/MySQL/mysql-connector-j-8.0.33.jar`
- **WHEN** PostgreSQL driver is downloaded
- **THEN** the system SHALL save to `drivers/PostgreSQL/postgresql-42.7.2.jar`

### Requirement: Plugin Driver Metadata

Plugins SHALL provide Maven coordinates for their required JDBC drivers to enable automatic download.

#### Scenario: Plugin provides Maven coordinates
- **WHEN** plugin implements ConnectionProvider capability
- **THEN** plugin SHALL provide method to get driver Maven coordinates (groupId, artifactId, version)
- **AND** coordinates SHALL be valid Maven Central coordinates
- **AND** version SHALL be specific (not snapshot or range)

#### Scenario: Plugin metadata used for download
- **WHEN** driver download is requested
- **THEN** the system SHALL query plugin for driver Maven coordinates
- **AND** query plugin for database type (DbType) to determine storage directory
- **AND** use coordinates to construct Maven Central download URL
- **AND** download driver using constructed URL
- **AND** save to database-type-specific directory

### Requirement: Unified Response Format

The system SHALL return all responses in unified ApiResponse format.

#### Scenario: Success response with driver path
- **WHEN** driver download succeeds
- **THEN** the system SHALL return ApiResponse with code=200, message="success"
- **AND** data object SHALL contain driver path and metadata (pluginId, databaseType, fileName, version)

#### Scenario: Success response with available drivers list
- **WHEN** GET /api/drivers/available succeeds
- **THEN** the system SHALL return ApiResponse with code=200, message="success"
- **AND** data object SHALL contain list of available drivers with metadata

#### Scenario: Success response with installed drivers list
- **WHEN** GET /api/drivers/installed succeeds
- **THEN** the system SHALL return ApiResponse with code=200, message="success"
- **AND** data object SHALL contain list of installed drivers with file details

#### Scenario: Success response with versions list
- **WHEN** GET /api/drivers/{pluginId}/versions succeeds
- **THEN** the system SHALL return ApiResponse with code=200, message="success"
- **AND** data object SHALL contain list of available versions from Maven Central
- **AND** each version entry SHALL indicate if it's installed locally

#### Scenario: Error response
- **WHEN** driver download fails
- **THEN** the system SHALL return ApiResponse with appropriate error code, error message, and data=null

