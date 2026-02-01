package org.mcuniverse.common;

import org.mcuniverse.rank.RankService;
import org.mcuniverse.rank.permission.RankPermissionFactory;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.MinestomLamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class LampFactory {

    public static Lamp<MinestomCommandActor> create(RankService rankService, LampExtension... extensions) {
        var builder = MinestomLamp.builder();

        // 권한 팩토리 등록
        builder.permissionFactory(new RankPermissionFactory(rankService));

        // 확장 기능(파라미터 타입, 자동완성 등) 일괄 등록
        for (LampExtension extension : extensions) {
            extension.register(builder);
        }

        return builder.build();
    }
}