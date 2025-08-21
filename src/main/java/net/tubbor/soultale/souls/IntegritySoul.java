package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

public class IntegritySoul {

    private static final EntityAttributeModifier INTEGRITY_LOW =
            new EntityAttributeModifier(
                    Identifier.of("soultale", "integrity_low"),
                    4, // +4 armor
                    EntityAttributeModifier.Operation.ADD_VALUE
            );

    private static final EntityAttributeModifier INTEGRITY_MID =
            new EntityAttributeModifier(
                    Identifier.of("soultale", "integrity_mid"),
                    2, // +2 armor
                    EntityAttributeModifier.Operation.ADD_VALUE
            );

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Soul check
                ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (data == null || !"Integrity".equals(data.soul())) continue;

                var attr = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
                if (attr == null) continue;

                attr.removeModifier(INTEGRITY_LOW.id());
                attr.removeModifier(INTEGRITY_MID.id());

                double health = player.getHealth();

                // Health check
                if (health <= 4) {
                    attr.addTemporaryModifier(INTEGRITY_LOW);   // +4 armor
                } else if (health <= 10) {
                    attr.addTemporaryModifier(INTEGRITY_MID);   // +2 armor
                }
            }
        });
    }
}
