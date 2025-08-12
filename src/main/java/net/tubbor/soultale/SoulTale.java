package net.tubbor.soultale;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;
import net.tubbor.soultale.command.SoulCommand;
import net.tubbor.soultale.souls.BraverySoul;
import net.tubbor.soultale.souls.DeterminationSoul;
import net.tubbor.soultale.souls.JusticeSoul;
import net.tubbor.soultale.souls.PatienceSoul;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

public class SoulTale implements ModInitializer {
	public static final String MOD_ID = "soultale";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String[] SOULS = {
			"Determination", "Kindness", "Justice", "Bravery",
			"Patience", "Integrity", "Perseverance", "Hate", "Fear"
	};
	public static final Map<String, Integer> SOUL_COLORS = Map.of(
			"Determination", 0xF51106,
			"Kindness", 0x26F606,
			"Justice", 0xFFD900,
			"Bravery", 0xFF9400,
			"Patience", 0x06DBF6,
			"Integrity", 0x0615F6,
			"Perseverance", 0xA600FF,
			"Hate", 0x151515,
			"Fear", 0xFF0080
	);

	@Override
	public void onInitialize() {

		//Registry

		ModAttachmentType.init();

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
			SoulCommand.register(commandDispatcher);
		}));

		//Soul Registry

		DeterminationSoul.register();
		BraverySoul.register();
		JusticeSoul.register();
		PatienceSoul.register();

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

				data = data.withSoul(chosenSoul);
				player.setAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE, data);

				int color = SOUL_COLORS.getOrDefault(chosenSoul, 0xFFFFFF);
				Text coloredSoul = Text.literal(chosenSoul).withColor(color);

				player.sendMessage(
						Text.literal("Starting a new world fills you with ")
								.append(coloredSoul)
								.append("!"),
						false
				);
			} else {
				player.sendMessage(Text.literal("Your soul is: " + currentSoul));
			}
		});
	}
}