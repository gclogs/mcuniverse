package org.mcuniverse.economy;

import java.math.BigDecimal;
import java.util.UUID;

public class EconomyService {

    private final EconomyStrategy strategy;

    // 생성자 주입 (Dependency Injection)
    public EconomyService(EconomyStrategy strategy) {
        this.strategy = strategy;
    }

    public void createAccount(UUID playerUuid) {
        if (!strategy.hasAccount(playerUuid)) {
            strategy.createAccount(playerUuid, BigDecimal.ZERO);
        }
    }

    public BigDecimal getBalance(UUID playerUuid) {
        return strategy.getBalance(playerUuid);
    }

    public boolean deposit(UUID playerUuid, double amount) {
        return strategy.deposit(playerUuid, BigDecimal.valueOf(amount));
    }

    public boolean withdraw(UUID playerUuid, double amount) {
        return strategy.withdraw(playerUuid, BigDecimal.valueOf(amount));
    }

    public void shutdown() {
        strategy.onShutdown();
    }
}