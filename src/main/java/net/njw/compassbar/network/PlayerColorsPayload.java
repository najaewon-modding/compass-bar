package net.njw.compassbar.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.njw.compassbar.CompassBar;

import java.util.List;

public record PlayerColorsPayload(List<PlayerColorData> colors) implements CustomPacketPayload {
    public static final Type<PlayerColorsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CompassBar.MODID, "player_colors"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<PlayerColorData>> COLOR_LIST_CODEC =
            PlayerColorData.STREAM_CODEC.apply(ByteBufCodecs.list(256));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerColorsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    COLOR_LIST_CODEC,
                    PlayerColorsPayload::colors,
                    PlayerColorsPayload::new
            );

    public PlayerColorsPayload {
        colors = List.copyOf(colors);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
