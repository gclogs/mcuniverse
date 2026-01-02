package org.mcuniverse;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.mcuniverse.listener.ConnectionListener;
import org.mcuniverse.island.manager.IslandManager;
import org.mcuniverse.island.commands.IslandCommand;
import org.mcuniverse.managers.SpawnManager;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 2, Block.GRASS_BLOCK));
        instanceContainer.setChunkSupplier(LightingChunk::new);

        Pos spawnPosition = new Pos(0, 2, 0);
        SpawnManager.setSpawn(instanceContainer, spawnPosition);

        // 관리자 객체 생성
        IslandManager islandManager = new IslandManager();

        // 이벤트 리스너 등록
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 2, 0));
        });
        new ConnectionListener(globalEventHandler);

        // 명령어 등록
        MinecraftServer.getCommandManager().register(new IslandCommand(islandManager));

        minecraftServer.start("0.0.0.0", 25565);
    }
}