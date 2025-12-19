package com.ofmesh.backend.user.progression.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "ofmesh.progression")
public class ProgressionProperties {
    private String internalToken;
    private List<Long> levelThresholds;
    private Map<String, Long> rules;

    public String getInternalToken() { return internalToken; }
    public void setInternalToken(String internalToken) { this.internalToken = internalToken; }

    public List<Long> getLevelThresholds() { return levelThresholds; }
    public void setLevelThresholds(List<Long> levelThresholds) { this.levelThresholds = levelThresholds; }

    public Map<String, Long> getRules() { return rules; }
    public void setRules(Map<String, Long> rules) { this.rules = rules; }
}
