package net.njw.compassbar.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.njw.compassbar.CompassBar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = CompassBar.MODID, value = Dist.CLIENT)
public final class PlayerListHud {
    private static final Identifier PLAYER_LIST_HUD =
            Identifier.fromNamespaceAndPath(CompassBar.MODID, "player_list_hud");

    private static final int RIGHT_MARGIN = 8;
    private static final int ROW_HEIGHT = 12;
    private static final int MARKER_SIZE = 5;
    private static final int MARKER_TEXT_GAP = 5;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private PlayerListHud() {}

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PLAYER_LIST_HUD, PlayerListHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.hideGui || !CompassState.isVisible() || minecraft.player == null
                || minecraft.getConnection() == null) return;

        List<PlayerInfo> players = new ArrayList<>(minecraft.getConnection().getOnlinePlayers());
        if (players.isEmpty()) return;

        var localUuid = minecraft.player.getUUID();

        players.sort(
                Comparator
                        .comparing((PlayerInfo player) -> !player.getProfile().id().equals(localUuid))
                        .thenComparing(
                                player -> player.getProfile().name(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        int maxNameWidth = 0;

        for (PlayerInfo player : players) {
            maxNameWidth = Math.max(maxNameWidth, minecraft.font.width(player.getProfile().name()));
        }

        int listWidth = MARKER_SIZE + MARKER_TEXT_GAP + maxNameWidth;
        int leftX = graphics.guiWidth() - RIGHT_MARGIN - listWidth;
        int listHeight = players.size() * ROW_HEIGHT;
        int startY = (graphics.guiHeight() - listHeight) / 2;
        int markerCenterX = leftX + MARKER_SIZE / 2;
        int textX = leftX + MARKER_SIZE + MARKER_TEXT_GAP;

        for (int index = 0; index < players.size(); index++) {
            PlayerInfo player = players.get(index);
            int rowY = startY + index * ROW_HEIGHT;
            int markerColor = PlayerColorManager.getOrAssignColor(player.getProfile().id());

            drawDiamondMarker(
                    graphics,
                    markerCenterX,
                    rowY + 7,
                    MARKER_SIZE,
                    markerColor
            );

            graphics.text(
                    minecraft.font,
                    player.getProfile().name(),
                    textX,
                    rowY + 1,
                    TEXT_COLOR,
                    true
            );
        }
    }

    private static void drawDiamondMarker(
            GuiGraphicsExtractor graphics,
            int centerX,
            int bottomY,
            int size,
            int color
    ) {
        int topY = bottomY - size + 1;

        for (int row = 0; row < size; row++) {
            int centerRow = size / 2;
            int rowWidth = size - 2 * Math.abs(row - centerRow);
            int left = centerX - rowWidth / 2;
            int y = topY + row;

            graphics.fill(left, y, left + rowWidth, y + 1, color);
        }
    }
}