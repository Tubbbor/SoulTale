package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

public class KindnessSoul {

    public static void register() {
        // AFTER_DAMAGE: Oyuncu birine vurduğunda tetiklenir
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, taken, blocked) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

            // Ruh kontrolü
            ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
            if (data == null || !"Kindness".equals(data.soul())) return;

            // Kendini iyileştir
            float healAmount = 1.0f; // yarım kalp
            player.heal(healAmount);

            // İsteğe bağlı: küçük kalp partikülleri göster
            player.getWorld().sendEntityStatus(player, (byte) 7); // vanilla kalp partikülü
        });
    }
}
