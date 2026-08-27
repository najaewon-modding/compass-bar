package net.njw.compassbar.network;

import net.njw.compassbar.server.PlayerPositionSync;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {
    private NetworkHandler() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(PlayerPositionsPayload.TYPE, PlayerPositionsPayload.STREAM_CODEC);
        registrar.playToServer(CompassSubscriptionPayload.TYPE, CompassSubscriptionPayload.STREAM_CODEC, (payload, context) -> PlayerPositionSync.setCompassActive((ServerPlayer) context.player(), payload.active()));
    }
}