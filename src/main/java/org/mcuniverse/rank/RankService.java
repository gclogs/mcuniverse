package org.mcuniverse.rank;

import java.util.UUID;

public class RankService {

    private final RankStrategy strategy;

    public RankService(RankStrategy strategy) {
        this.strategy = strategy;
    }

    public void createRank(UUID playerUuid) {
        if (!strategy.hasRank(playerUuid)) {
            strategy.createRank(playerUuid, Rank.NEWBIE);
        }
    }

    public Rank getRank(UUID playerUuid) {
        return strategy.getRank(playerUuid);
    }

    public void setRank(UUID playerUuid, Rank rank) {
        strategy.setRank(playerUuid, rank);
    }

    public void shutdown() {
        strategy.onShutdown();
    }
}
