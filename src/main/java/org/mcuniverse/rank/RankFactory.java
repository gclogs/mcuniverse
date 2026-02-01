package org.mcuniverse.rank;

import org.mcuniverse.rank.impl.InMemoryRankStrategy;

public class RankFactory {

    public enum StorageType {
        MEMORY,
        MYSQL,
        JSON
    }

    public static RankStrategy createStrategy(StorageType type) {
        switch (type) {
            case MEMORY:
                return new InMemoryRankStrategy();
            case MYSQL:
                // return new MySqlRankStrategy(...);
                throw new UnsupportedOperationException("MySQL implementation not yet available");
            default:
                throw new IllegalArgumentException("Unknown storage type: " + type);
        }
    }
}
