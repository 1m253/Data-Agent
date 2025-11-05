package edu.zsc.ai.config;

import edu.zsc.ai.plugin.manager.DefaultPluginManager;
import edu.zsc.ai.plugin.manager.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Plugin Configuration
 * Configures PluginManager as Spring Bean.
 *
 * @author Data-Agent
 * @since 0.0.1
 */
@Configuration
public class PluginConfig {
    
    /**
     * Create PluginManager bean.
     * Uses DefaultPluginManager which loads plugins via Java SPI.
     *
     * @return PluginManager instance
     */
    @Bean
    public PluginManager pluginManager() {
        return new DefaultPluginManager();
    }
}

