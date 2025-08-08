package net.tubbor.soultale;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;
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

		//Registry

		ModAttachmentType.init();

		//Join events

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;

			ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);

			if (data == null) {
				data = ModCustomAttachedData.DEFAULT;
				player.setAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE, data);
			}

			String currentSoul = data.soul();

			if (currentSoul.equals("none")) {
				Random random = new Random();
				int index = random.nextInt(SOULS.length);
				String chosenSoul = SOULS[index];

				// Set the soul for the player
				data = data.withSoul(chosenSoul);
				player.setAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE, data);

				// Notify the player
				player.sendMessage(Text.literal("Starting a new world fills you with " + chosenSoul + "!"), false);
			} else {
				player.sendMessage(Text.literal("Your current soul is: " + currentSoul), false);
			}
		});
	}
}