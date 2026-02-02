package org.mcuniverse.economy.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.bson.Document;
import org.mcuniverse.common.database.DatabaseManager;
import org.mcuniverse.economy.EconomyAccount;
import org.mcuniverse.economy.EconomyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoEconomyStrategy implements EconomyStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MongoEconomyStrategy.class);

    private final MongoCollection<Document> collection;
    private final RedisCommands<String, String> redis;
    private static final String KEY_PREFIX = "economy:account:";

    public MongoEconomyStrategy() {
        this.collection = DatabaseManager.getInstance().getMongoDatabase().getCollection("economy");
        this.redis = DatabaseManager.getInstance().getRedisSync();
    }

    private String getKey(UUID uuid) {
        return KEY_PREFIX + uuid.toString();
    }

    /**
     * DB에서 economy account 정보를 모두 불러와 Redis Hash에 저장합니다.
     */
    private Map<String, String> loadAndCacheFromDb(UUID uuid) {
        Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
        if (doc != null) {
            Map<String, String> data = new HashMap<>();

            // 1. 필요한 필드들을 다 가져와서 Map에 담습니다.
            data.put(EconomyAccount.BALANCE.getFieldName(), String.valueOf(doc.get(EconomyAccount.BALANCE.getFieldName(), 0L)));
            data.put(EconomyAccount.CASH.getFieldName(), String.valueOf(doc.get(EconomyAccount.CASH.getFieldName(), 0L)));

            // 2. Redis Hash에 전체 저장
            redis.hset(getKey(uuid), data);
            return data;
        }
        return null;
    }

    @Override
    public void createAccount(UUID uuid, long initialAmount) {
        String key = getKey(uuid);

        if (redis.exists(key) == 0) {
            if (collection.countDocuments(Filters.eq("uuid", uuid.toString())) == 0) {
                // Redis Hash 초기 데이터
                Map<String, String> initialData = new HashMap<>();
                initialData.put(EconomyAccount.BALANCE.getFieldName(), String.valueOf(initialAmount));
                initialData.put(EconomyAccount.CASH.getFieldName(), String.valueOf(initialAmount));

                redis.hset(key, initialData);

                // DB 비동기 저장
                CompletableFuture.runAsync(() -> {
                    Document doc = new Document("uuid", uuid.toString())
                            .append("balance", initialAmount)
                            .append("cash", initialAmount);
                    collection.insertOne(doc);
                }, DatabaseManager.getInstance().getDbExecutor())
                .exceptionally(e -> {
                    logger.error("DB 저장 실패", e); // DB 저장 실패 시 로그 출력 필수
                    return null;
                });
            } else {
                loadAndCacheFromDb(uuid);
            }
        }
    }

    @Override
    public long getAccount(UUID uuid, EconomyAccount filedName) {
        return getField(uuid, filedName);
    }

    @Override
    public boolean deposit(UUID uuid, EconomyAccount filedName, long amount) {
        return modifyField(uuid, filedName, amount);
    }

    @Override
    public boolean withdraw(UUID uuid, EconomyAccount filedName, long amount) {
        return modifyField(uuid, filedName, -amount);
    }

    @Override
    public void setAccount(UUID uuid, EconomyAccount filedName, long amount) {
        setField(uuid, filedName.getFieldName(), amount);
    }

    /**
     * 특정 필드 값 가져오기 (Redis -> DB fallback)
     */
    private long getField(UUID uuid, EconomyAccount fieldName) {
        String key = getKey(uuid);
        String cached = redis.hget(key, fieldName.getFieldName());

        if (cached != null) {
            return Long.parseLong(cached);
        }

        Map<String, String> data = loadAndCacheFromDb(uuid);
        // [Fix] fieldName 객체 자체가 아닌 String 키를 사용해야 함
        return data != null ? Long.parseLong(data.getOrDefault(fieldName.getFieldName(), "0")) : 0L;
    }

    /**
     * 특정 필드 값 수정하기 (증가/감소) + Lua Script로 원자성(Atomicity) 보장
     */
    private boolean modifyField(UUID uuid, EconomyAccount fieldName, long amount) {
        String key = getKey(uuid);
        String field = fieldName.getFieldName();

        // 1. 데이터 로드 확인 (없으면 로드)
        if (redis.exists(key) == 0 && loadAndCacheFromDb(uuid) == null) {
            return false;
        }

        // 2. Lua Script 작성
        // 로직: "현재 잔액 가져와서(없으면 0), 더했을 때 음수면 실패(0 리턴), 아니면 변경하고 성공(1 리턴)"
        String script = """
                    local current = redis.call('HGET', KEYS[1], ARGV[1])
                    if not current then current = 0 end
                
                    local val = tonumber(current)
                    local change = tonumber(ARGV[2])
                
                    -- 잔액 부족 확인 (출금 시)
                    if val + change < 0 then return 0 end
                
                    -- 값 변경
                    redis.call('HINCRBY', KEYS[1], ARGV[1], ARGV[2])
                    return 1
                """;

        // 3. 스크립트 실행 (이 부분은 절대 쪼개지지 않는 원자적 연산입니다)
        boolean success = redis.eval(
                script,
                ScriptOutputType.BOOLEAN,
                new String[]{key}, // KEYS[1]
                field,             // ARGV[1]
                String.valueOf(amount) // ARGV[2]
        );

        if (!success) {
            return false; // 잔액 부족으로 거절됨
        }

        // 4. Redis 성공 시에만 DB 비동기 업데이트 진행
        CompletableFuture.runAsync(() -> {
            if (amount < 0) {
                // 출금 시 DB 안전장치 (2중 방어)
                collection.updateOne(
                        Filters.and(Filters.eq("uuid", uuid.toString()), Filters.gte(field, -amount)),
                        Updates.inc(field, amount)
                );
            } else {
                // 입금
                collection.updateOne(
                        Filters.eq("uuid", uuid.toString()),
                        Updates.inc(field, amount)
                );
            }
        }, DatabaseManager.getInstance().getDbExecutor());

        return true;
    }

    /**
     * 특정 필드 값 덮어쓰기
     */
    private void setField(UUID uuid, String fieldName, long value) {
        String key = getKey(uuid);
        redis.hset(key, fieldName, String.valueOf(value));

        CompletableFuture.runAsync(() -> {
            collection.updateOne(
                    Filters.eq("uuid", uuid.toString()),
                    Updates.set(fieldName, value)
            );
        }, DatabaseManager.getInstance().getDbExecutor());
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        String key = getKey(uuid);
        if (redis.exists(key) > 0) return true;
        return loadAndCacheFromDb(uuid) != null;
    }
}