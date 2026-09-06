package net.njw.compassbar.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record PlayerColorData(UUID uuid, int rgb) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerColorData> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    PlayerColorData::uuid,
                    ByteBufCodecs.VAR_INT,
                    PlayerColorData::rgb,
                    PlayerColorData::new
            );
}
