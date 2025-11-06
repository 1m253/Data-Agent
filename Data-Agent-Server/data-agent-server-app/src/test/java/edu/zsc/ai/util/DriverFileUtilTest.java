package edu.zsc.ai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DriverFileUtil.
 */
class DriverFileUtilTest {
    
    @Test
    void testExtractVersionFromFileName_StandardFormat() {
        String fileName = "mysql-connector-j-8.0.33.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("8.0.33", version);
    }
    
    @Test
    void testExtractVersionFromFileName_DifferentArtifact() {
        String fileName = "postgresql-42.7.2.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("42.7.2", version);
    }
    
    @Test
    void testExtractVersionFromFileName_SingleDigitVersion() {
        String fileName = "some-driver-1.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("1", version);
    }
    
    @Test
    void testExtractVersionFromFileName_ComplexVersion() {
        String fileName = "driver-1.2.3.4.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("1.2.3.4", version);
    }
    
    @Test
    void testExtractVersionFromFileName_NoVersion() {
        String fileName = "driver.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("unknown", version);
    }
    
    @Test
    void testExtractVersionFromFileName_NullFileName() {
        String version = DriverFileUtil.extractVersionFromFileName(null);
        assertEquals("unknown", version);
    }
    
    @Test
    void testExtractVersionFromFileName_EmptyFileName() {
        String version = DriverFileUtil.extractVersionFromFileName("");
        assertEquals("unknown", version);
    }
    
    @Test
    void testExtractVersionFromFileName_NotJarFile() {
        String fileName = "driver-8.0.33.zip";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        // Pattern expects .jar extension
        assertEquals("unknown", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithBetaSuffix() {
        String fileName = "mysql-connector-j-8.0.33-beta.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        // Should extract version with suffix
        assertEquals("8.0.33", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithSnapshotSuffix() {
        String fileName = "postgresql-42.7.2-SNAPSHOT.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("42.7.2", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithAlphaSuffix() {
        String fileName = "driver-1.0.0-alpha.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("1.0.0", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithRCSuffix() {
        String fileName = "driver-2.1.0-RC1.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("2.1.0", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithM1Suffix() {
        String fileName = "driver-3.0.0-M1.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("3.0.0", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithRCSuffixNoHyphen() {
        String fileName = "driver-2.1.0RC1.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("2.1.0", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithSnapshotSuffixNoHyphen() {
        String fileName = "driver-1.0.0SNAPSHOT.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("1.0.0", version);
    }
    
    @Test
    void testExtractVersionFromFileName_WithBetaSuffixNoHyphen() {
        String fileName = "mysql-connector-j-8.0.33beta.jar";
        String version = DriverFileUtil.extractVersionFromFileName(fileName);
        assertEquals("8.0.33", version);
    }
}

