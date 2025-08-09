package net.tubbor.soultale.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private static final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(String key, UUID playerId, long cooldownMillis) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> map = cooldowns.get(key);

        if (map != null && map.containsKey(playerId)) {
            long lastUse = map.get(playerId);
            if (now - lastUse < cooldownMillis) {
                return true; // hâlâ cooldown’da
            }
        }
        return false; // cooldown bitmiş
    }

    public static long getRemainingCooldown(String key, UUID playerId, long cooldownMillis) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> map = cooldowns.get(key);

        if (map != null && map.containsKey(playerId)) {
            long lastUse = map.get(playerId);
            long remaining = cooldownMillis - (now - lastUse);
            return Math.max(0, remaining);
        }
        return 0;
    }

    public static void setCooldown(String key, UUID playerId) {
        cooldowns.computeIfAbsent(key, k -> new HashMap<>())
                .put(playerId, System.currentTimeMillis());
    }
}
