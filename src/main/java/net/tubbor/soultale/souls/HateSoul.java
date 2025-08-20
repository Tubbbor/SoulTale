package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

import java.util.*;

public class HateSoul {
    private static final Map<UUID, HateData> HATE_STACKS = new HashMap<>();
    private static final int STACK_DURATION = 20 * 60 * 10; // 10dk tick
    private static final Identifier HATE_MODIFIER_ID = Identifier.of("soultale", "hate_bonus");

    public static void register() {
        // Tick - süreleri kontrol et
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            long tick = server.getOverworld().getTime();
            Iterator<Map.Entry<UUID, HateData>> it = HATE_STACKS.entrySet().iterator();

            while (it.hasNext()) {
                var entry = it.next();
                HateData data = entry.getValue();
                if (tick > data.expireTick) {
                    it.remove();
                }
            }

            // Oyuncuların attack modifier'ını güncelle
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ModCustomAttachedData soulData = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (soulData == null || !"Hate".equals(soulData.soul())) continue;

                int stacks = getStacks(player);
                var attr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (attr == null) continue;

                attr.removeModifier(HATE_MODIFIER_ID);
                if (stacks > 0) {
                    double bonus = 0.05 * stacks; // %5 stack başına
                    EntityAttributeModifier mod = new EntityAttributeModifier(
                            HATE_MODIFIER_ID,
                            bonus,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    );
                    attr.addTemporaryModifier(mod);
                }
            }
        });

        // Player öldüğünde stack resetle
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity victim) {
                clearStacks(victim); // Ölen oyuncunun Hate stackleri sıfırlanır
            }

            if (source.getAttacker() instanceof ServerPlayerEntity killer) {
                ModCustomAttachedData soulData = killer.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (soulData != null && "Hate".equals(soulData.soul())) {
                    onPlayerKill(killer); // Stack ekle ve heal ver
                    killer.sendMessage(Text.literal("You fed your Hate..."), false);
                }
            }
        });

        // Canlıya vurulduğunda Wither ekle
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, taken, blocked) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

            ModCustomAttachedData soulData = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
            if (soulData == null || !"Hate".equals(soulData.soul())) return;

            if (player.getHealth() <= player.getMaxHealth() / 2f) {
                if (entity instanceof LivingEntity target) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
                }
            }
        });
    }

    // Kill eventinden çağırılacak
    public static void onPlayerKill(ServerPlayerEntity killer) {
        long currentTick = killer.getWorld().getTime();
        HATE_STACKS.compute(killer.getUuid(), (uuid, old) -> {
            if (old == null) {
                return new HateData(1, currentTick + STACK_DURATION);
            } else {
                int newStacks = Math.min(old.stacks + 1, 10);
                return new HateData(newStacks, currentTick + STACK_DURATION);
            }
        });

        killer.heal(3f); // 1.5 kalp yenile
    }

    public static int getStacks(ServerPlayerEntity player) {
        HateData data = HATE_STACKS.get(player.getUuid());
        return (data == null) ? 0 : data.stacks;
    }

    public static void clearStacks(ServerPlayerEntity player) {
        HATE_STACKS.remove(player.getUuid());
    }

    private static class HateData {
        int stacks;
        long expireTick;

        HateData(int stacks, long expireTick) {
            this.stacks = stacks;
            this.expireTick = expireTick;
        }
    }
}
