package org.mcuniverse.rank;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.common.GameFeature;
import org.mcuniverse.rank.commands.RankCommand;
import org.mcuniverse.rank.permission.RankPermissionFactory;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class RankFeature implements GameFeature {

    private RankService rankService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        // 서비스가 아직 없으면 생성 (Main에서 미리 호출했을 수 있음)
        if (this.rankService == null) {
            RankStrategy strategy = RankFactory.createStrategy(RankFactory.StorageType.MEMORY);
            this.rankService = new RankService(strategy);
        }

        // Lamp가 전달되었을 때만 명령어 등록
        if (lamp != null) {
            lamp.register(new RankCommand(rankService));
        }
    }

    @Override
    public void disable(MinecraftServer server) {
        if (rankService != null) {
            rankService.shutdown();
        }
    }
    
    public RankService getRankService() {
        return rankService;
    }
}