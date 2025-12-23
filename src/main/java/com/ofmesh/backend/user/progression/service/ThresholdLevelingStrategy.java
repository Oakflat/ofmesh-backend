package com.ofmesh.backend.user.progression.service;

import java.util.List;

public class ThresholdLevelingStrategy implements LevelingStrategy {

    private final List<Long> thresholds; // index 0 => level 1 threshold

    public ThresholdLevelingStrategy(List<Long> thresholds) {
        this.thresholds = thresholds;
    }

    @Override
    public int levelForXp(long xpTotal) {
        if (thresholds == null || thresholds.isEmpty()) return 1;
        int lvl = 1;
        for (int i = 0; i < thresholds.size(); i++) {
            if (xpTotal >= thresholds.get(i)) lvl = i + 1;
            else break;
        }
        return lvl;
    }

    @Override
    public long nextLevelXp(int currentLevel) {
        if (thresholds == null || thresholds.isEmpty()) return -1;
        int idx = currentLevel; // next level threshold index
        if (idx >= thresholds.size()) return -1;
        return thresholds.get(idx);
    }
}
