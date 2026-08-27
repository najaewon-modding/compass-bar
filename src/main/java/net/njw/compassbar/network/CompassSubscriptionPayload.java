package net.njw.compassbar.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.njw.compassbar.CompassBar;

public record CompassSubscriptionPayload(boolean active) implements CustomPacketPayload {
    public static final Type<CompassSubscriptionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(CompassBar.MODID, "compass_subscription"));
    public static final StreamCodec<ByteBuf, CompassSubscriptionPayload> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(CompassSubscriptionPayload::new, CompassSubscriptionPayload::active);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}