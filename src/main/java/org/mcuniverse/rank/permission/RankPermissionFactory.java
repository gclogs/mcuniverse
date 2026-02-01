package org.mcuniverse.rank.permission;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcuniverse.rank.Rank;
import org.mcuniverse.rank.RankService;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class RankPermissionFactory implements CommandPermission.Factory<MinestomCommandActor> {

    private final RankService rankService;

    public RankPermissionFactory(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    @Nullable
    public CommandPermission<MinestomCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<MinestomCommandActor> lamp) {
        RequiresRank requiresRank = annotations.get(RequiresRank.class);
        if (requiresRank == null) return null;

        Rank requiredRank = requiresRank.value();

        return actor -> {
            // 콘솔은 항상 허용
            if (!(actor.sender() instanceof Player player)) {
                return true;
            }
            // 플레이어의 현재 랭크 레벨이 요구 랭크 레벨보다 높거나 같으면 통과
            return rankService.getRank(player.getUuid()).getLevel() >= requiredRank.getLevel();
        };
    }
}