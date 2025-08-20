package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.tubbor.soultale.SoulTale;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

import java.util.*;

public class PerseveranceSoul {
    // Track modified effects to avoid re-processing
    private static final Map<UUID, Set<String>> processedEffects = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (data == null || !"Perseverance".equals(data.soul())) {
                    processedEffects.remove(player.getUuid());
                    continue;
                }

                processPlayerEffects(player);
            }
        });
    }

    private static void processPlayerEffects(ServerPlayerEntity player) {
        Set<String> processed = processedEffects.computeIfAbsent(player.getUuid(), id -> new HashSet<>());
        List<StatusEffectInstance> effectsToModify = new ArrayList<>();

        // Collect effects that need modification
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            if (effect.isInfinite()) continue;

            String key = effect.getEffectType().getKey()
                    .map(k -> k.getValue().toString())
                    .orElse("unknown");

            // Only process each effect once when it's first applied
            if (!processed.contains(key)) {
                StatusEffectCategory category = effect.getEffectType().value().getCategory();

                if (category == StatusEffectCategory.BENEFICIAL || category == StatusEffectCategory.HARMFUL) {
                    effectsToModify.add(effect);
                    processed.add(key);
                }
            }
        }

        // Apply modifications
        for (StatusEffectInstance effect : effectsToModify) {
            StatusEffectCategory category = effect.getEffectType().value().getCategory();
            int newDuration = effect.getDuration();

            if (category == StatusEffectCategory.BENEFICIAL) {
                // Positive effects last 2x longer
                newDuration = effect.getDuration() * 2;
                SoulTale.LOGGER.info("Perseverance: Extended positive effect {} from {} to {} ticks",
                        effect.getEffectType().getKey().map(k -> k.getValue().toString()).orElse("unknown"),
                        effect.getDuration(), newDuration);
            } else if (category == StatusEffectCategory.HARMFUL) {
                // Negative effects last 1/2 time
                newDuration = effect.getDuration() / 2;
                SoulTale.LOGGER.info("Perseverance: Reduced negative effect {} from {} to {} ticks",
                        effect.getEffectType().getKey().map(k -> k.getValue().toString()).orElse("unknown"),
                        effect.getDuration(), newDuration);
            }

            // Replace the effect with modified duration
            if (newDuration != effect.getDuration() && newDuration > 0) {
                player.removeStatusEffect(effect.getEffectType());
                player.addStatusEffect(new StatusEffectInstance(
                        effect.getEffectType(),
                        newDuration,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.shouldShowParticles(),
                        effect.shouldShowIcon()
                ));
            }
        }

        // Clean up processed effects that no longer exist
        Set<String> currentEffects = new HashSet<>();
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            String key = effect.getEffectType().getKey()
                    .map(k -> k.getValue().toString())
                    .orElse("unknown");
            currentEffects.add(key);
        }
        processed.retainAll(currentEffects);
    }
}