package net.tubbor.soultale;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.tubbor.soultale.component.SoulComponent;
import net.tubbor.soultale.component.SoulTaleComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SoulTale implements ModInitializer {
	public static final String MOD_ID = "soultale";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String[] SOULS = {
			"Determination", "Kindness", "Justice", "Bravery",
			"Patience", "Integrity", "Perseverance", "Hate", "Fear"
	};

	@Override
	public void onInitialize() {


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			SoulComponent soul = SoulTaleComponents.SOUL.get(player);

			PlayerAdvancementTracker tracker = player.getAdvancementTracker();
			Identifier id = Identifier.of("soultale", "has_soul");
			AdvancementEntry advancement = server.getAdvancementLoader().get(id);

			if (advancement == null) {
				System.out.println("Advancement could not be loaded!");
				return;
			}

			AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);

			if(soul.getSoul() == null) {
				Random random = new Random();
				int index = random.nextInt(SOULS.length);
				String chosenSoul = SOULS[index];
				soul.setSoul(chosenSoul);
				player.getAdvancementTracker().grantCriterion(advancement, "has_soul");

				player.sendMessage(Text.literal("Starting a new world fills you with " + chosenSoul + "!"), false);
			} else {
				soul.setSoul(soul.getSoul());
			}
		});
	}
}