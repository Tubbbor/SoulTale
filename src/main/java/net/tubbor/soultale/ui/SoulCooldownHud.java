package net.tubbor.soultale.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;
import net.tubbor.soultale.utils.CooldownManager;

public class SoulCooldownHud implements HudRenderCallback {

    private String formatCooldownTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return (minutes > 0) ? String.format("%dm %ds", minutes, seconds) : String.format("%ds", seconds);
    }

    private static final Identifier DETERMINATION_ICON = Identifier.of("soultale", "textures/gui/determination.png");
    private static final Identifier KINDESS_ICON = Identifier.of("soultale", "textures/gui/kindness.png");
    private static final Identifier BRAVERY_ICON = Identifier.of("soultale", "textures/gui/bravery.png");
    private static final Identifier JUSTICE_ICON = Identifier.of("soultale", "textures/gui/justice.png");
    private static final Identifier PATIENCE_ICON = Identifier.of("soultale", "textures/gui/patience.png");
    private static final Identifier INTEGRITY_ICON = Identifier.of("soultale", "textures/gui/integrity.png");
    private static final Identifier PERSEVERANCE_ICON = Identifier.of("soultale", "textures/gui/perseverance.png");
    private static final Identifier HATE_ICON = Identifier.of("soultale", "textures/gui/hate.png");
    private static final Identifier FEAR_ICON = Identifier.of("soultale", "textures/gui/fear.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ModCustomAttachedData data = client.player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
        String soul = data.soul();

        Identifier iconTexture = getTextureForSoul(soul);
        int x = drawContext.getScaledWindowWidth() / 2 - 8;
        int y = drawContext.getScaledWindowHeight() - 50;

        RenderSystem.enableBlend();
        drawContext.drawTexture(iconTexture, x, y, 0, 0, 16, 16, 16, 16);

        // Sadece Determination için cooldown göster
        if (soul.equals("Determination")) {
            long remaining = CooldownManager.getRemainingCooldown(
                    "determination_death_save",
                    client.player.getUuid(),
                    15 * 60 * 1000
            );

            if (remaining > 0) {
                String timeLeft = formatCooldownTime(remaining);
                int textWidth = client.textRenderer.getWidth(timeLeft);
                int textX = x + (16 - textWidth) / 2;
                int textY = y - 10;
                drawContext.drawTextWithShadow(client.textRenderer, timeLeft, textX, textY, 0xFFFFFF);
            }
        }
    }

    private Identifier getTextureForSoul(String soul) {
        return switch (soul) {
            case "Determination" -> DETERMINATION_ICON;
            case "Kindness" -> KINDESS_ICON;
            case "Bravery" -> BRAVERY_ICON;
            case "Justice" -> JUSTICE_ICON;
            case "Patience" -> PATIENCE_ICON;
            case "Integrity" -> INTEGRITY_ICON;
            case "Perseverance" -> PERSEVERANCE_ICON;
            case "Hate" -> HATE_ICON;
            case "Fear" -> FEAR_ICON;
            default -> Identifier.of("soultale", "textures/gui/none.png"); // Varsayılan ikon

        };
    }
}
