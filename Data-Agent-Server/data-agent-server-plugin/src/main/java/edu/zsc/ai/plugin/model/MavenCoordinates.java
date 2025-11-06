package edu.zsc.ai.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maven coordinates for a JDBC driver.
 * Contains groupId, artifactId, and version information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MavenCoordinates {
    
    /**
     * Maven group ID (e.g., "com.mysql")
     */
    private String groupId;
    
    /**
     * Maven artifact ID (e.g., "mysql-connector-j")
     */
    private String artifactId;
    
    /**
     * Maven version (e.g., "8.0.33")
     */
    private String version;
    
    /**
     * Get full Maven coordinate string.
     *
     * @return coordinate string in format "groupId:artifactId:version"
     */
    public String toCoordinateString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }
    
    /**
     * Check if coordinates are complete (all fields are non-null and non-empty).
     *
     * @return true if all fields are present
     */
    public boolean isComplete() {
        return groupId != null && !groupId.isEmpty()
            && artifactId != null && !artifactId.isEmpty()
            && version != null && !version.isEmpty();
    }
}

