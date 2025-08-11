package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

public class BraverySoul {

    // Tek seferlik tanımlanan modifier'lar
    private static final EntityAttributeModifier BRAVERY_LOW =
            new EntityAttributeModifier(
                    Identifier.of("soultale", "bravery_low"),
                    1.0, // +100% damage
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final EntityAttributeModifier BRAVERY_MID =
            new EntityAttributeModifier(
                    Identifier.of("soultale", "bravery_mid"),
                    0.5, // +50% damage
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                // Oyuncunun ruhu Bravery değilse atla
                ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (data == null || !"Bravery".equals(data.soul())) continue;

                var attr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (attr == null) continue;

                double health = player.getHealth();

                // Önce tüm Bravery modifier'larını kaldır
                attr.removeModifier(BRAVERY_LOW.id());
                attr.removeModifier(BRAVERY_MID.id());

                // Can seviyesine göre buff ekle
                if (health <= 4) {
                    attr.addTemporaryModifier(BRAVERY_LOW);   // x2 damage
                } else if (health <= 10) {
                    attr.addTemporaryModifier(BRAVERY_MID);   // x1.5 damage
                }
            }
        });
    }
}
