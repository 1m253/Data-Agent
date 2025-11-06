package edu.zsc.ai.plugin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MavenCoordinates.
 */
class MavenCoordinatesTest {
    
    @Test
    void testToCoordinateString() {
        MavenCoordinates coordinates = new MavenCoordinates(
            "com.mysql",
            "mysql-connector-j",
            "8.0.33"
        );
        
        assertEquals("com.mysql:mysql-connector-j:8.0.33", coordinates.toCoordinateString());
    }
    
    @Test
    void testIsComplete_AllFieldsPresent() {
        MavenCoordinates coordinates = new MavenCoordinates(
            "com.mysql",
            "mysql-connector-j",
            "8.0.33"
        );
        
        assertTrue(coordinates.isComplete());
    }
    
    @Test
    void testIsComplete_MissingGroupId() {
        MavenCoordinates coordinates = new MavenCoordinates(
            null,
            "mysql-connector-j",
            "8.0.33"
        );
        
        assertFalse(coordinates.isComplete());
    }
    
    @Test
    void testIsComplete_EmptyGroupId() {
        MavenCoordinates coordinates = new MavenCoordinates(
            "",
            "mysql-connector-j",
            "8.0.33"
        );
        
        assertFalse(coordinates.isComplete());
    }
    
    @Test
    void testIsComplete_MissingArtifactId() {
        MavenCoordinates coordinates = new MavenCoordinates(
            "com.mysql",
            null,
            "8.0.33"
        );
        
        assertFalse(coordinates.isComplete());
    }
    
    @Test
    void testIsComplete_MissingVersion() {
        MavenCoordinates coordinates = new MavenCoordinates(
            "com.mysql",
            "mysql-connector-j",
            null
        );
        
        assertFalse(coordinates.isComplete());
    }
}

