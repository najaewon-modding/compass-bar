package net.njw.compassbar.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.njw.compassbar.CompassBar;
import net.njw.compassbar.network.PlayerPositionData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = CompassBar.MODID, value = Dist.CLIENT)
public final class CompassHud {
    private static final Identifier COMPASS_HUD =
            Identifier.fromNamespaceAndPath(CompassBar.MODID, "compass_hud");

    private static final int LABEL_Y = 4;
    private static final int TICK_BOTTOM_Y = 23;
    private static final int TICK_SPACING = 10;
    private static final int HALF_TICKS = 12;
    private static final float DEGREES_PER_TICK = 11.25F;
    private static final float LABEL_VISIBLE_ANGLE = 95.0F;
    private static final int CENTER_TICK_HEIGHT = 8;
    private static final int MARKER_BOTTOM_Y = 16;

    private static final double NEAR_DISTANCE = 64.0;
    private static final double MEDIUM_DISTANCE = 256.0;

    private static final int WHITE_TEXT_COLOR = 0xFFFFFFFF;
    private static final int X_AXIS_TEXT_COLOR = 0xFFFFB8B8;
    private static final int Z_AXIS_TEXT_COLOR = 0xFFD8B8FF;
    private static final int MAJOR_TICK_COLOR = 0xFFFFFFFF;
    private static final int MEDIUM_TICK_COLOR = 0xDDFFFFFF;
    private static final int MINOR_TICK_COLOR = 0xAAFFFFFF;
    private static final int CENTER_TICK_COLOR = 0xCCFFFFFF;

    private enum PlayerMarkerSize {
        FAR(3),
        MEDIUM(4),
        NEAR(5);

        private final int size;

        PlayerMarkerSize(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    private CompassHud() {}

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(COMPASS_HUD, CompassHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.options.hideGui || !CompassState.isVisible()) return;

        int centerX = graphics.guiWidth() / 2;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        float heading = normalizeDegrees(camera.yRot() + 180.0F);

        drawCompass(graphics, minecraft, centerX, heading);
        drawTick(graphics, centerX, TICK_BOTTOM_Y, 2, CENTER_TICK_HEIGHT, CENTER_TICK_COLOR);
        drawPlayerMarkers(graphics, minecraft, centerX, heading);
    }

    private static void drawCompass(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            int centerX,
            float heading
    ) {
        int baseTick = (int) Math.floor(heading / DEGREES_PER_TICK);
        int halfWidth = HALF_TICKS * TICK_SPACING;

        for (int offset = -HALF_TICKS - 1; offset <= HALF_TICKS + 1; offset++) {
            int tickIndex = baseTick + offset;
            float tickAngle = tickIndex * DEGREES_PER_TICK;
            float relativeAngle = Mth.wrapDegrees(tickAngle - heading);
            float screenOffset = relativeAngle / DEGREES_PER_TICK * TICK_SPACING;
            int x = Math.round(centerX + screenOffset);
            int distanceFromCenter = Math.abs(x - centerX);

            if (distanceFromCenter > halfWidth) continue;

            int normalizedTick = Math.floorMod(tickIndex, 32);
            float edgeRatio = Math.min(1.0F, distanceFromCenter / (float) halfWidth);

            int tickHeight;
            int tickColor;

            if (normalizedTick % 4 == 0) {
                tickHeight = Math.max(4, Math.round(6.0F - 2.0F * edgeRatio));
                tickColor = MAJOR_TICK_COLOR;
            } else if (normalizedTick % 2 == 0) {
                tickHeight = Math.max(3, Math.round(4.0F - edgeRatio));
                tickColor = MEDIUM_TICK_COLOR;
            } else {
                tickHeight = 3;
                tickColor = MINOR_TICK_COLOR;
            }

            drawTick(graphics, x, TICK_BOTTOM_Y, 1, tickHeight, tickColor);

            if (normalizedTick % 4 == 0 && Math.abs(relativeAngle) <= LABEL_VISIBLE_ANGLE) {
                String label = getDirectionLabel(normalizedTick);
                drawLabel(graphics, minecraft, label, x, LABEL_Y, getDirectionLabelColor(label));
            }
        }
    }

    private static void drawPlayerMarkers(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            int centerX,
            float cameraHeading
    ) {
        var localPlayer = minecraft.player;
        if (localPlayer == null) return;

        long renderTimeNanos = System.nanoTime();

        for (PlayerPositionData target : PlayerPositionCache.getPlayers()) {
            if (target.uuid().equals(localPlayer.getUUID())) continue;
            if (!target.dimension().equals(localPlayer.level().dimension())) continue;

            PlayerPositionCache.InterpolatedPosition interpolatedPosition =
                    PlayerPositionCache.getInterpolatedPosition(target.uuid());

            double targetX;
            double targetZ;

            if (interpolatedPosition != null && interpolatedPosition.dimension().equals(target.dimension())) {
                targetX = interpolatedPosition.x(renderTimeNanos);
                targetZ = interpolatedPosition.z(renderTimeNanos);
            } else {
                targetX = target.x();
                targetZ = target.z();
            }

            double dx = targetX - localPlayer.getX();
            double dz = targetZ - localPlayer.getZ();
            double distanceSquared = dx * dx + dz * dz;

            if (distanceSquared < 0.0001) continue;

            float playerHeading = normalizeDegrees((float) Math.toDegrees(Math.atan2(dx, -dz)));
            PlayerMarkerSize markerSize = getPlayerMarkerSize(distanceSquared);
            int markerColor = PlayerColorManager.getOrAssignColor(target.uuid());

            drawPlayerMarker(graphics, centerX, cameraHeading, playerHeading, markerSize, markerColor);
        }
    }

    private static void drawPlayerMarker(
            GuiGraphicsExtractor graphics,
            int centerX,
            float cameraHeading,
            float playerHeading,
            PlayerMarkerSize markerSize,
            int markerColor
    ) {
        float relativeAngle = Mth.wrapDegrees(playerHeading - cameraHeading);
        float maxVisibleAngle = HALF_TICKS * DEGREES_PER_TICK;

        if (Math.abs(relativeAngle) > maxVisibleAngle) return;

        float screenOffset = relativeAngle / DEGREES_PER_TICK * TICK_SPACING;
        int x = Math.round(centerX + screenOffset);

        drawDiamondMarker(graphics, x, MARKER_BOTTOM_Y, markerSize.size(), markerColor);
    }

    private static PlayerMarkerSize getPlayerMarkerSize(double distanceSquared) {
        if (distanceSquared <= NEAR_DISTANCE * NEAR_DISTANCE) return PlayerMarkerSize.NEAR;
        if (distanceSquared <= MEDIUM_DISTANCE * MEDIUM_DISTANCE) return PlayerMarkerSize.MEDIUM;
        return PlayerMarkerSize.FAR;
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
            int rowWidth;

            if (size % 2 == 1) {
                int centerRow = size / 2;
                rowWidth = size - 2 * Math.abs(row - centerRow);
            } else {
                int distanceFromEdge = Math.min(row, size - 1 - row);
                rowWidth = 2 * (distanceFromEdge + 1);
            }

            int left = centerX - rowWidth / 2;
            int y = topY + row;
            graphics.fill(left, y, left + rowWidth, y + 1, color);
        }
    }

    private static String getDirectionLabel(int normalizedTick) {
        return switch (normalizedTick / 4) {
            case 0 -> "N";
            case 1 -> "NE";
            case 2 -> "E";
            case 3 -> "SE";
            case 4 -> "S";
            case 5 -> "SW";
            case 6 -> "W";
            case 7 -> "NW";
            default -> "";
        };
    }

    private static int getDirectionLabelColor(String label) {
        return switch (label) {
            case "E", "W" -> X_AXIS_TEXT_COLOR;
            case "N", "S" -> Z_AXIS_TEXT_COLOR;
            default -> WHITE_TEXT_COLOR;
        };
    }

    private static void drawTick(
            GuiGraphicsExtractor graphics,
            int centerX,
            int bottomY,
            int width,
            int height,
            int color
    ) {
        int left = centerX - width / 2;
        graphics.fill(left, bottomY - height, left + width, bottomY, color);
    }

    private static void drawLabel(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            String text,
            int x,
            int y,
            int color
    ) {
        graphics.centeredText(minecraft.font, Component.literal(text), x, y, color);
    }

    private static float normalizeDegrees(float degrees) {
        degrees %= 360.0F;
        if (degrees < 0.0F) degrees += 360.0F;
        return degrees;
    }
}