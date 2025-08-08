package net.tubbor.soultale.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ModCustomAttachedData(String soul) {
    public static final Codec<ModCustomAttachedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("soul").forGetter(ModCustomAttachedData::soul)
    ).apply(instance, ModCustomAttachedData::new));

    public static final PacketCodec<ByteBuf, ModCustomAttachedData> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public static final ModCustomAttachedData DEFAULT = new ModCustomAttachedData("none");

    public ModCustomAttachedData withSoul(String soul) {
        return new ModCustomAttachedData(soul);
    }

    public ModCustomAttachedData clear() {
        return DEFAULT;
    }
}
