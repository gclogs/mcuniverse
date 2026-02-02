package org.mcuniverse.rank;

public enum Rank {
    NEWBIE("뉴비", 1),
    MEMBER("멤버", 2),
    VIP("VIP", 3),
    ADMIN("관리자", 99);

    private final String displayName;
    private final int level;

    Rank(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }
}