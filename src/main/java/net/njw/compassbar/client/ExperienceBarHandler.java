package net.njw.compassbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.njw.compassbar.CompassBar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = CompassBar.MODID, value = Dist.CLIENT)
public final class ExperienceBarHandler {
    private static ExperienceBarRenderer experienceBarRenderer;

    private ExperienceBarHandler() {}

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.gameMode == null || minecraft.options.hideGui) return;

        if (event.getName().equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND)) {
            event.setCanceled(true);

            if (minecraft.gameMode.hasExperience()) {
                getExperienceBarRenderer(minecraft).extractBackground(
                        event.getGuiGraphics(),
                        event.getPartialTick()
                );
            }

            return;
        }

        if (event.getName().equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR)) {
            event.setCanceled(true);

            if (minecraft.gameMode.hasExperience()) {
                getExperienceBarRenderer(minecraft).extractRenderState(
                        event.getGuiGraphics(),
                        event.getPartialTick()
                );
            }
        }
    }

    private static ExperienceBarRenderer getExperienceBarRenderer(Minecraft minecraft) {
        if (experienceBarRenderer == null) {
            experienceBarRenderer = new ExperienceBarRenderer(minecraft);
        }

        return experienceBarRenderer;
    }
}