package net.njw.compassbar.client;

import net.njw.compassbar.CompassBar;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(
        modid = CompassBar.MODID,
        value = Dist.CLIENT
)
public final class CompassToggleHandler {

    private static boolean tabWasDown = false;

    private CompassToggleHandler() {
    }

    // ------------------------------------------------------------
    // Tab Toggle
    // ------------------------------------------------------------

    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        boolean tabIsDown =
                minecraft.options
                        .keyPlayerList
                        .isDown();

        /*
         * false -> true가 되는 순간만 press로 취급한다.
         *
         * 따라서 Tab을 길게 누르고 있어도
         * 한 번만 toggle된다.
         */
        boolean tabPressed =
                tabIsDown && !tabWasDown;

        /*
         * 반드시 매 tick 갱신한다.
         */
        tabWasDown = tabIsDown;

        if (!tabPressed) {
            return;
        }

        /*
         * 월드 안에서만 동작.
         */
        if (minecraft.player == null) {
            return;
        }

        /*
         * Inventory, Chat 등의 Screen이 열려 있을 때는
         * Tab toggle을 처리하지 않는다.
         */
        if (minecraft.screen != null) {
            return;
        }

        CompassState.toggle();
    }

    // ------------------------------------------------------------
    // Hide Vanilla Tab List
    // ------------------------------------------------------------

    @SubscribeEvent
    public static void onRenderGuiLayer(
            RenderGuiLayerEvent.Pre event
    ) {
        if (
                event.getName().equals(
                        VanillaGuiLayers.TAB_LIST
                )
        ) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------

    @SubscribeEvent
    public static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {
        CompassState.hide();
        tabWasDown = false;
    }
}