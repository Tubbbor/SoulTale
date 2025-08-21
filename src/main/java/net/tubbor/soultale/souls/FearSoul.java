package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

public class FearSoul {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, taken, blocked) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

            // Soul check
            ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
            if (data == null || !"Fear".equals(data.soul())) return;

            if (entity instanceof LivingEntity target) {
                //Apply slowness
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0));

                //Apply weakness if player is below half hp
                if (player.getHealth() <= player.getMaxHealth() / 2f) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0));
                }
            }
        });
    }
}
