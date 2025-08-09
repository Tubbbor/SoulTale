package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;
import net.tubbor.soultale.utils.CooldownManager;

public class DeterminationSoul {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register(((entity, damageSource, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }

            ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);

            if (!data.soul().equalsIgnoreCase("Determination")) {
                return true;
            }

            String cooldownKey = "determination_death_save";
            long cooldown =  60 * 1000; // 1dk

            if (CooldownManager.isOnCooldown(cooldownKey, player.getUuid(), cooldown)) {
                return true;
            }

            player.setHealth(player.getMaxHealth());
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 4));

            player.getServerWorld().spawnParticles(
                    ParticleTypes.HEART,
                    player.getX(), player.getBodyY(1.0), player.getZ(),
                    20,
                    0.5, 0.5, 0.5,
                    0.1
            );

            player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0F, 0.8F);

            CooldownManager.setCooldown(cooldownKey, player.getUuid());

            return false;
        }));
    }
}
