package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.tubbor.soultale.SoulTale;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

import java.util.*;

public class PatienceSoul {
    private static final Map<UUID, List<DelayedDamage>> DELAYED_DAMAGE = new HashMap<>();
    private static final int DELAY_TICKS = 60; // 3 seconds (20 tick * 3)
    private static final Set<UUID> PROCESSING_DAMAGE = new HashSet<>(); // Prevent recursion

    public static void register() {
        // Handle incoming damage
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Only handle player damage
            if (!(entity instanceof ServerPlayerEntity player)) return true;

            // Prevent recursive damage calls
            if (PROCESSING_DAMAGE.contains(player.getUuid())) return true;

            // Check if player has Patience soul
            ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
            if (data == null || !"Patience".equals(data.soul())) return true;

            // Don't process magic damage (our delayed damage) or void damage
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.MAGIC) ||
                    source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
                return true;
            }

            // Split damage
            float immediate = amount * 0.5f; // 50% immediate
            float delayed = amount * 0.5f;   // 50% delayed

            try {
                PROCESSING_DAMAGE.add(player.getUuid());

                // Apply immediate damage
                if (immediate > 0) {
                    player.damage(source, immediate);
                }

                // Schedule delayed damage
                DELAYED_DAMAGE.computeIfAbsent(player.getUuid(), id -> new ArrayList<>())
                        .add(new DelayedDamage(player, delayed, DELAY_TICKS, source));

                SoulTale.LOGGER.info("Patience soul: Split damage {} into immediate {} and delayed {} for player {}",
                        amount, immediate, delayed, player.getName().getString());

            } finally {
                PROCESSING_DAMAGE.remove(player.getUuid());
            }

            // Cancel the original damage since we handled it manually
            return false;
        });

        // Apply delayed damage
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, List<DelayedDamage>>> mapIterator = DELAYED_DAMAGE.entrySet().iterator();

            while (mapIterator.hasNext()) {
                var entry = mapIterator.next();
                List<DelayedDamage> damageList = entry.getValue();

                Iterator<DelayedDamage> damageIterator = damageList.iterator();
                while (damageIterator.hasNext()) {
                    DelayedDamage delayedDamage = damageIterator.next();
                    delayedDamage.ticks--;

                    if (delayedDamage.ticks <= 0) {
                        // Check if player is still valid
                        ServerPlayerEntity player = delayedDamage.target;
                        if (player == null || player.isRemoved() || !player.isAlive()) {
                            damageIterator.remove();
                            continue;
                        }

                        // Check if player still has patience soul
                        ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                        if (data == null || !"Patience".equals(data.soul())) {
                            damageIterator.remove();
                            continue;
                        }

                        try {
                            PROCESSING_DAMAGE.add(player.getUuid());

                            // Apply delayed damage with armor calculation but no hitstun/knockback
                            float finalDamage = Math.min(delayedDamage.amount, player.getHealth() - 0.5f); // Leave at least 0.5 health
                            if (finalDamage > 0) {
                                // Calculate armor-reduced damage using the original damage source
                                DamageSource magicDamage = player.getWorld().getDamageSources().magic();
                                float armorReducedDamage = net.minecraft.entity.DamageUtil.getDamageLeft(
                                        player, finalDamage, magicDamage, player.getArmor(), (float)player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
                                );

                                // Apply enchantment protection (like Protection, Magic Protection)
                                armorReducedDamage = net.minecraft.entity.DamageUtil.getInflictedDamage(armorReducedDamage, player.getStatusEffects().size());

                                // Apply the calculated damage directly to health
                                if (armorReducedDamage > 0) {
                                    float newHealth = player.getHealth() - armorReducedDamage;
                                    player.setHealth(Math.max(newHealth, 0.5f));

                                    // Play hurt sound
                                    player.playSoundToPlayer(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_HURT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                                }
                            }

                        } catch (Exception e) {
                            SoulTale.LOGGER.error("Error applying delayed damage to player {}: {}",
                                    player.getName().getString(), e.getMessage());
                        } finally {
                            PROCESSING_DAMAGE.remove(player.getUuid());
                        }

                        damageIterator.remove();
                    }
                }

                // Remove empty lists
                if (damageList.isEmpty()) {
                    mapIterator.remove();
                }
            }
        });
    }

    private static class DelayedDamage {
        final ServerPlayerEntity target;
        final float amount;
        int ticks;
        final DamageSource originalSource;

        DelayedDamage(ServerPlayerEntity target, float amount, int ticks, DamageSource originalSource) {
            this.target = target;
            this.amount = amount;
            this.ticks = ticks;
            this.originalSource = originalSource;
        }
    }
}