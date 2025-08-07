package net.tubbor.soultale.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.tubbor.soultale.SoulTale;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementProvider extends FabricAdvancementProvider {
    public AdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {

        AdvancementEntry hasSoul = Advancement.Builder.create()
                .display(
                        Items.HEART_OF_THE_SEA,
                        Text.literal("You have a soul!"),
                        Text.literal("Idk, you just have a soul now."),
                        Identifier.ofVanilla("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementFrame.TASK,
                        false, // showToast
                        false, // announceToChat
                        true // hidden
                )
                .criterion("has_soul", InventoryChangedCriterion.Conditions.items(Items.HEART_OF_THE_SEA)) // Trigger yerine geçici çözüm
                .build(consumer, SoulTale.MOD_ID + ":has_soul");

    }
}
