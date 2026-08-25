package net.njw.compassbar.network;

import net.njw.compassbar.CompassBar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record PlayerPositionsPayload(
        List<PlayerPositionData> players
) implements CustomPacketPayload {

    public static final Type<PlayerPositionsPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            CompassBar.MODID,
                            "player_positions"
                    )
            );

    private static final StreamCodec<
            RegistryFriendlyByteBuf,
            List<PlayerPositionData>
            > PLAYER_LIST_CODEC =
            PlayerPositionData.STREAM_CODEC.apply(
                    ByteBufCodecs.list(256)
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            PlayerPositionsPayload
            > STREAM_CODEC =
            StreamCodec.composite(
                    PLAYER_LIST_CODEC,
                    PlayerPositionsPayload::players,
                    PlayerPositionsPayload::new
            );

    public PlayerPositionsPayload {
        players = List.copyOf(players);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}