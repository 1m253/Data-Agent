package edu.zsc.ai.enums;

/**
 * Connection test status enumeration
 *
 * @author Data-Agent
 * @since 0.0.1
 */
public enum ConnectionTestStatus {
    
    /**
     * Connection test succeeded
     */
    SUCCEEDED("Succeeded"),
    
    /**
     * Connection test failed
     */
    FAILED("Failed");
    
    private final String displayName;
    
    ConnectionTestStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

