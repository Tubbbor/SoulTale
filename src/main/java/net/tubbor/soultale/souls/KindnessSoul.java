package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

public class KindnessSoul {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, taken, blocked) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

            // Soul check
            ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
            if (data == null || !"Kindness".equals(data.soul())) return;

            // Heal yourself
            float healAmount = 1.0f;
            player.heal(healAmount);
        });
    }
}
