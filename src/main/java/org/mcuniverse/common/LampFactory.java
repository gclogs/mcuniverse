package org.mcuniverse.common;

import org.mcuniverse.economy.Currency;
import org.mcuniverse.economy.CurrencyParameterType;
import org.mcuniverse.rank.RankGroup;
import org.mcuniverse.rank.RankParameterType;
import org.mcuniverse.rank.RankService;
import org.mcuniverse.rank.permission.RankPermissionFactory;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.MinestomLamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class LampFactory {

    public static Lamp<MinestomCommandActor> create(RankService rankService, LampExtension... extensions) {
        var builder = MinestomLamp.builder();

        builder.permissionFactory(new RankPermissionFactory(rankService));
        builder.parameterTypes(params -> {
            params.addParameterType(RankGroup.class, new RankParameterType());
        });

        builder.parameterTypes(params -> {
            params.addParameterType(Currency.class, new CurrencyParameterType());
        });

        for (LampExtension extension : extensions) {
            extension.register(builder);
        }

        return builder.build();
    }
}