package org.mcuniverse.common.managers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

public class SpawnManager {
    private static InstanceContainer spawnInstance;
    private static Pos spawnPosition;
    
    public static void setSpawn(InstanceContainer instance, Pos position) {
        spawnInstance = instance;
        spawnPosition = position;
    }
    
    public static InstanceContainer getSpawnInstance() {
        return spawnInstance;
    }
    
    public static Pos getSpawnPosition() {
        return spawnPosition != null ? spawnPosition : new Pos(0, 2, 0);
    }
    
    /**
     * 플레이어를 메인 spawn으로 이동시킵니다.
     */
    public static void teleportToSpawn(Player player) {
        if (spawnInstance != null) {
            // 현재 인스턴스와 이동하려는 인스턴스가 다른 경우에만 setInstance 호출
            if (player.getInstance() != spawnInstance) {
                player.setInstance(spawnInstance);
            }
            // 같은 인스턴스에 있으면 teleport만 호출
            player.teleport(getSpawnPosition());
        }
    }
}