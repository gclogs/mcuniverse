package org.mcuniverse.rank.impl;

import org.mcuniverse.rank.Rank;
import org.mcuniverse.rank.RankStrategy;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 기반의 고성능 랭크 구현체입니다
 * ConcurrentHashMap을 사용하여 스레드 안전성을 보장합니다.
 */
public class InMemoryRankStrategy implements RankStrategy {

    public final Map<UUID, Rank> ranks = new ConcurrentHashMap<>();

    @Override
    public boolean hasRank(UUID uuid) {
        return ranks.containsKey(uuid);
    }

    @Override
    public void createRank(UUID uuid, Rank rank) {
        ranks.putIfAbsent(uuid, rank);
    }

    @Override
    public Rank getRank(UUID uuid) {
        return ranks.getOrDefault(uuid, Rank.NEWBIE);
    }

    @Override
    public void setRank(UUID uuid, Rank rank) {
        ranks.put(uuid, rank);
    }

    @Override
    public void onShutdown() {
        // 메모리 방식은 프로세스 종료 시 OS가 메모리를 회수하므로 별도 작업이 필요 없습니다.
    }
}
