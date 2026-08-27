package net.njw.compassbar.client;

import net.minecraft.client.Minecraft;
import net.njw.compassbar.CompassBar;
import net.njw.compassbar.network.CompassSubscriptionPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = CompassBar.MODID, value = Dist.CLIENT)
public final class CompassToggleHandler {
    private static boolean tabWasDown = false;

    private CompassToggleHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean tabIsDown = minecraft.options.keyPlayerList.isDown();
        boolean tabPressed = tabIsDown && !tabWasDown;
        tabWasDown = tabIsDown;

        if (!tabPressed || minecraft.player == null || minecraft.screen != null) return;

        CompassState.toggle();
        boolean visible = CompassState.isVisible();
        sendSubscriptionState(minecraft, visible);

        if (visible) {
            PlayerColorManager.assignMissingColors(
                    PlayerPositionCache.getPlayers(),
                    minecraft.player.getUUID()
            );
        } else {
            PlayerPositionCache.clear();
        }
    }

    private static void sendSubscriptionState(Minecraft minecraft, boolean active) {
        var connection = minecraft.getConnection();
        if (connection == null || !connection.hasChannel(CompassSubscriptionPayload.TYPE)) return;
        ClientPacketDistributor.sendToServer(new CompassSubscriptionPayload(active));
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.TAB_LIST)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CompassState.hide();
        PlayerPositionCache.clear();
        tabWasDown = false;
    }
}