package org.mcuniverse.common.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;

import java.util.UUID;

public class ConnectionListener {

    // 생성자에서 전역 핸들러를 받아 리스너 등록
    public ConnectionListener(GlobalEventHandler handler) {
        // 람다식 대신 'this::메소드명' 형태의 메소드 참조 사용
        handler.addListener(AsyncPlayerPreLoginEvent.class, this::onLogin);
        handler.addListener(PlayerSpawnEvent.class, this::onSpawn);
    }

    // 플레이어 비동기 로그인 이벤트 (로그인 핸드셰이크)
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getGameProfile().name();
        UUID uuid = event.getGameProfile().uuid();
        System.out.println("[접속 시도] " + name + " (" + uuid + ")");
    }

    // 플레이어 스폰 시 실행될 로직
    private void onSpawn(PlayerSpawnEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("서버에 오신 것을 환영합니다!");
    }
}