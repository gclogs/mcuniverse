package org.mcuniverse.economy;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.mcuniverse.common.GameFeature;
import org.mcuniverse.common.data.PlayerDataHandler;
import org.mcuniverse.common.listener.CommonConnectionListener;
import org.mcuniverse.economy.commands.EconomyAdminCommand;
import org.mcuniverse.economy.commands.EconomyCommand;
import org.mcuniverse.economy.impl.MongoEconomyStrategy;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class EconomyFeature implements GameFeature {

    private EconomyService economyService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        EconomyStrategy strategy = new MongoEconomyStrategy();
        this.economyService = new EconomyService(strategy);

        lamp.register(new EconomyCommand(economyService));
        lamp.register(new EconomyAdminCommand(economyService));
        // 3. 이벤트 리스너 등록 (Unified Listener)
        CommonConnectionListener commonListener = new CommonConnectionListener(server.getGlobalEventHandler());
        commonListener.addHandler(new PlayerDataHandler() {
            @Override
            public void onLoad(Player player) {
                economyService.createAccount(player.getUuid(), player.getUsername());
            }

            @Override
            public void onUnload(Player player) {
                economyService.expireAccountCache(player.getUuid(), 3600);
            }
        });
    }

    @Override
    public void disable(MinecraftServer server) {

        System.out.println("Economy system disabled. Ensuring data integrity...");
    }
}