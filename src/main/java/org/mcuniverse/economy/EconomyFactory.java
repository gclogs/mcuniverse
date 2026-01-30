package org.mcuniverse.economy;

import org.mcuniverse.economy.impl.InMemoryEconomyStrategy;

public class EconomyFactory {

    public enum StorageType {
        MEMORY,
        MYSQL, // 추후 구현 가능
        JSON   // 추후 구현 가능
    }

    public static EconomyStrategy createStrategy(StorageType type) {
        switch (type) {
            case MEMORY:
                return new InMemoryEconomyStrategy();
            case MYSQL:
                // return new MySqlEconomyStrategy(...);
                throw new UnsupportedOperationException("MySQL implementation not yet available");
            default:
                throw new IllegalArgumentException("Unknown storage type: " + type);
        }
    }
}