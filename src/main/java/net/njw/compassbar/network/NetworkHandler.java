package net.njw.compassbar.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {

    private NetworkHandler() {
    }

    public static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        PayloadRegistrar registrar =
                event.registrar("1").optional();

        registrar.playToClient(
                PlayerPositionsPayload.TYPE,
                PlayerPositionsPayload.STREAM_CODEC
        );
    }
}