package net.tubbor.soultale;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.tubbor.soultale.ui.SoulCooldownHud;

public class SoulTaleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new SoulCooldownHud());
    }
}
