package edu.zsc.ai.plugin.connection;

import edu.zsc.ai.plugin.exception.PluginErrorCode;
import edu.zsc.ai.plugin.exception.PluginException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Utility class for validating JAR files.
 */
public final class JarFileValidator {
    
    private static final Logger logger = Logger.getLogger(JarFileValidator.class.getName());
    
    /**
     * JAR file magic number (first 4 bytes: 0x50 0x4B 0x03 0x04)
     */
    private static final byte[] JAR_MAGIC_NUMBER = {0x50, 0x4B, 0x03, 0x04};
    
    private JarFileValidator() {
        // Utility class
    }
    
    /**
     * Validate that a file is a valid JAR file.
     *
     * @param filePath path to the file to validate
     * @throws PluginException if validation fails
     */
    public static void validate(Path filePath) throws PluginException {
        if (filePath == null) {
            throw new PluginException(PluginErrorCode.CONNECTION_FAILED, "File path is null");
        }
        
        // Check file exists
        if (!Files.exists(filePath)) {
            throw new PluginException(PluginErrorCode.CONNECTION_FAILED,
                "File does not exist: " + filePath);
        }
        
        // Check file is not empty
        try {
            if (Files.size(filePath) == 0) {
                throw new PluginException(PluginErrorCode.CONNECTION_FAILED,
                    "Downloaded file is empty: " + filePath);
            }
        } catch (IOException e) {
            throw new PluginException(PluginErrorCode.CONNECTION_FAILED,
                "Failed to check file size: " + filePath, e);
        }
        
        // Check JAR magic number
        try {
            byte[] header = new byte[4];
            try (var inputStream = Files.newInputStream(filePath)) {
                int bytesRead = inputStream.read(header);
                if (bytesRead != 4) {
                    throw new PluginException(PluginErrorCode.CONNECTION_FAILED,
                        "File is too small to be a valid JAR: " + filePath);
                }
            }
            
            // Verify magic number
            for (int i = 0; i < 4; i++) {
                if (header[i] != JAR_MAGIC_NUMBER[i]) {
                    throw new PluginException(PluginErrorCode.CONNECTION_FAILED,
                        "File does not appear to be a valid JAR file (invalid magic number): " + filePath);
                }
            }
            
            // Try to open as JAR file to verify structure
            try (JarFile jarFile = new JarFile(filePath.toFile())) {
                // If we can open it, it's a valid JAR
                logger.fine("JAR file validation passed: " + filePath);
            }
            
        } catch (IOException e) {
            throw new PluginException(PluginErrorCode.CONNECTION_FAILED,
                "Failed to validate JAR file: " + filePath + ". Error: " + e.getMessage(), e);
        }
    }
}

