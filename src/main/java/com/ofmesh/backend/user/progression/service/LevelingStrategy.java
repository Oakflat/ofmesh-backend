package com.ofmesh.backend.user.progression.service;

public interface LevelingStrategy {
    int levelForXp(long xpTotal);
    long nextLevelXp(int currentLevel);
}
