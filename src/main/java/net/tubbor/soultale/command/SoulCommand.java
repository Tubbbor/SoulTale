package net.tubbor.soultale.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.tubbor.soultale.SoulTale;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

import java.util.Arrays;

import static net.tubbor.soultale.SoulTale.SOUL_COLORS;

public class SoulCommand {

    // Define error messages
    private static final SimpleCommandExceptionType INVALID_SOUL_ERROR =
            new SimpleCommandExceptionType(Text.literal("Invalid soul type!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("soul")
                        .then(
                                CommandManager.literal("set")
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .then(
                                                CommandManager.argument("target", EntityArgumentType.player())
                                                        .then(
                                                                CommandManager.argument("soul", StringArgumentType.word())
                                                                        .suggests(((commandContext, suggestionsBuilder) -> {
                                                                            for (String soul : SoulTale.SOULS) {
                                                                                suggestionsBuilder.suggest(soul);
                                                                            }
                                                                            return suggestionsBuilder.buildFuture();
                                                                        }))
                                                                        .executes(context -> {
                                                                            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
                                                                            String newSoul = StringArgumentType.getString(context, "soul");

                                                                            // Validate soul type (case-insensitive)
                                                                            boolean isValidSoul = Arrays.stream(SoulTale.SOULS)
                                                                                    .anyMatch(validSoul -> validSoul.equalsIgnoreCase(newSoul));

                                                                            if (!isValidSoul) {
                                                                                throw INVALID_SOUL_ERROR.create();
                                                                            }

                                                                            // Find the correct case version of the soul
                                                                            String correctCaseSoul = Arrays.stream(SoulTale.SOULS)
                                                                                    .filter(validSoul -> validSoul.equalsIgnoreCase(newSoul))
                                                                                    .findFirst()
                                                                                    .orElse(newSoul);

                                                                            ModCustomAttachedData data = targetPlayer.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                                                                            data = data.withSoul(correctCaseSoul);
                                                                            targetPlayer.setAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE, data);

                                                                            int color = SOUL_COLORS.getOrDefault(correctCaseSoul, 0xFFFFFF);
                                                                            Text coloredSoul = Text.literal(correctCaseSoul).withColor(color);
                                                                            targetPlayer.sendMessage(Text.literal("Your soul has been set to ").append(coloredSoul), false);

                                                                            context.getSource().sendFeedback(() -> Text.literal("Set " + targetPlayer.getName().getString() + "'s soul to " + correctCaseSoul), true);

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );

        dispatcher.register(
                CommandManager.literal("soul")
                        .then(
                                CommandManager.literal("view")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            ModCustomAttachedData data = player.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                                            String soul = data.soul();

                                            int color = SOUL_COLORS.getOrDefault(soul, 0xFFFFFF);
                                            Text coloredSoul = Text.literal(soul).withColor(color);
                                            player.sendMessage(Text.literal("Your current soul is: ").append(coloredSoul), false);

                                            return 1;
                                        })
                        )
        );
    }
}