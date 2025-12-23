package com.ofmesh.backend.user.progression.dto;

public class ProgressionDTO {
    private Long userId;
    private long xpTotal;
    private int level;
    private long nextLevelXp; // -1 = 满级

    public static ProgressionDTO of(Long userId, long xpTotal, int level, long nextLevelXp) {
        ProgressionDTO dto = new ProgressionDTO();
        dto.userId = userId;
        dto.xpTotal = xpTotal;
        dto.level = level;
        dto.nextLevelXp = nextLevelXp;
        return dto;
    }

    public Long getUserId() { return userId; }
    public long getXpTotal() { return xpTotal; }
    public int getLevel() { return level; }
    public long getNextLevelXp() { return nextLevelXp; }
}
