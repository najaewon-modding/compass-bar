package net.njw.compassbar.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record PlayerPositionData(
        UUID uuid,
        ResourceKey<Level> dimension,
        double x,
        double z
) {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerPositionData> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    PlayerPositionData::uuid,

                    ResourceKey.streamCodec(Registries.DIMENSION),
                    PlayerPositionData::dimension,

                    ByteBufCodecs.DOUBLE,
                    PlayerPositionData::x,

                    ByteBufCodecs.DOUBLE,
                    PlayerPositionData::z,

                    PlayerPositionData::new
            );
}