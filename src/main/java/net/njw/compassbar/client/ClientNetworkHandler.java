package net.njw.compassbar.client;

import net.njw.compassbar.CompassBar;
import net.njw.compassbar.network.PlayerPositionsPayload;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(
        modid = CompassBar.MODID,
        value = Dist.CLIENT
)
public final class ClientNetworkHandler {

    private ClientNetworkHandler() {
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(
            RegisterClientPayloadHandlersEvent event
    ) {
        event.register(
                PlayerPositionsPayload.TYPE,
                (payload, context) ->
                        context.enqueueWork(
                                () -> PlayerPositionCache.update(
                                        payload.players()
                                )
                        )
        );
    }

    @SubscribeEvent
    public static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {
        PlayerPositionCache.clear();
    }
}