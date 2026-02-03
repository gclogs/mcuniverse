package org.mcuniverse.economy;

import java.util.UUID;

public class EconomyService {

    private final EconomyStrategy strategy;

    // 생성자 주입 (Dependency Injection)
    public EconomyService(EconomyStrategy strategy) {
        this.strategy = strategy;
    }

    public void createAccount(UUID uuid, String name) {
        if (!strategy.hasAccount(uuid)) {
            strategy.createAccount(uuid, name, 0L);
        }
    }

    public long getAccount(UUID uuid, EconomyAccount filedName) {
        return strategy.getAccount(uuid, filedName);
    }

    public boolean deposit(UUID uuid, EconomyAccount filedName, long amount) {
        return strategy.deposit(uuid, filedName, amount);
    }

    public boolean withdraw(UUID uuid, EconomyAccount filedName, long amount) {
        return strategy.withdraw(uuid, filedName, amount);
    }

    public void setAccount(UUID uuid, EconomyAccount filedName, long amount) {
        strategy.setAccount(uuid, filedName, amount);
    }

    public void deleteAccount(UUID uuid) {
        strategy.deleteAccount(uuid);
    }

    public void expireAccountCache(UUID uuid, long seconds) {
        strategy.expireAccountCache(uuid, seconds);
    }
}