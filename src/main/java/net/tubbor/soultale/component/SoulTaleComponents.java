package net.tubbor.soultale.component;

import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

public class SoulTaleComponents implements EntityComponentInitializer {

    public static final ComponentKey<SoulComponent> SOUL =
            ComponentRegistry.getOrCreate(
                    Identifier.of("soultale", "soul"),
                    SoulComponent.class
            );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(SOUL, player -> new SoulComponent(player));
    }
}