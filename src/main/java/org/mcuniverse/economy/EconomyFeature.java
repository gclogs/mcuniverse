package org.mcuniverse.economy;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.common.GameFeature;
import org.mcuniverse.common.config.ConfigManager;
import org.mcuniverse.economy.commands.EconomyAdminCommand;
import org.mcuniverse.economy.commands.EconomyCommand;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class EconomyFeature implements GameFeature {

    private EconomyService economyService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        // 1. 전략 및 서비스 초기화
        String typeStr = ConfigManager.get("storage.economy.type", "MEMORY");
        EconomyFactory.StorageType type;
        try {
            type = EconomyFactory.StorageType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid economy storage type: " + typeStr + ". Defaulting to MEMORY.");
            type = EconomyFactory.StorageType.JSON;
        }

        EconomyStrategy strategy = EconomyFactory.createStrategy(type);
        this.economyService = new EconomyService(strategy);

        // 2. 명령어 등록
        lamp.register(new EconomyCommand(economyService));
        lamp.register(new EconomyAdminCommand(economyService));

        // 3. 이벤트 리스너 (접속 시 계정 생성)
        server.getGlobalEventHandler().addListener(net.minestom.server.event.player.AsyncPlayerConfigurationEvent.class, event -> {
            economyService.createAccount(event.getPlayer().getUuid());
        });
    }

    @Override
    public void disable(MinecraftServer server) {

        System.out.println("Economy system disabled. Ensuring data integrity...");
    }
}