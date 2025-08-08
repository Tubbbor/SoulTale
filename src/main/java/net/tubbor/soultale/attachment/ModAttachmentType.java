package net.tubbor.soultale.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public class ModAttachmentType {
    public static final AttachmentType<ModCustomAttachedData> SOUL_ATTACHMENT_TYPE = AttachmentRegistry.create(
            Identifier.of("soultale", "soul_attachment"),
            builder -> builder
                    .initializer(() -> ModCustomAttachedData.DEFAULT)
                    .persistent(ModCustomAttachedData.CODEC)
                    .syncWith(
                            ModCustomAttachedData.PACKET_CODEC,
                            AttachmentSyncPredicate.all()
                    )
    );

    public static void init() {

    }
}
