package net.njw.compassbar.client;

import net.njw.compassbar.CompassBar;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(
        modid = CompassBar.MODID,
        value = Dist.CLIENT
)
public final class CompassHud {

    private static final Identifier COMPASS_HUD =
            Identifier.fromNamespaceAndPath(
                    CompassBar.MODID,
                    "compass_hud"
            );

    // ------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------

    private static final int LABEL_Y = 4;
    private static final int TICK_BOTTOM_Y = 23;

    /*
     * 작은 tick 하나 사이의 화면상 간격.
     *
     * 4 ticks = 45°
     * N <-> NE = 40px
     */
    private static final int TICK_SPACING = 10;

    /*
     * 중앙 기준 좌우 표시 tick 수.
     *
     * 전체 폭:
     * 12 * 10 * 2 = 240px
     */
    private static final int HALF_TICKS = 12;

    /*
     * 작은 tick 하나당 실제 각도.
     */
    private static final float DEGREES_PER_TICK = 11.25F;

    /*
     * Direction label 표시 범위.
     */
    private static final float LABEL_VISIBLE_ANGLE = 95.0F;

    /*
     * 화면 중앙 고정 indicator.
     */
    private static final int CENTER_TICK_HEIGHT = 8;

    /*
     * 모든 Player marker는 크기와 관계없이
     * 아래쪽을 이 위치에 맞춘다.
     */
    private static final int MARKER_BOTTOM_Y = 16;

    // ------------------------------------------------------------
    // Colors
    // ------------------------------------------------------------

    private static final int WHITE_TEXT_COLOR = 0xFFFFFFFF;

    // X Axis: E / W
    private static final int X_AXIS_TEXT_COLOR = 0xFFFFB8B8;

    // Z Axis: N / S
    private static final int Z_AXIS_TEXT_COLOR = 0xFFD8B8FF;

    private static final int MAJOR_TICK_COLOR = 0xFFFFFFFF;
    private static final int MEDIUM_TICK_COLOR = 0xDDFFFFFF;
    private static final int MINOR_TICK_COLOR = 0xAAFFFFFF;

    /*
     * 화면 중앙 indicator.
     *
     * 일반 major tick보다 약간 투명하게 표시.
     */
    private static final int CENTER_TICK_COLOR = 0xCCFFFFFF;

    /*
     * Player marker.
     *
     * 추후 player별 색상 등을 적용할 수도 있다.
     */
    private static final int PLAYER_MARKER_COLOR = 0xFFFFD966;

    // ------------------------------------------------------------
    // Player Marker Size
    // ------------------------------------------------------------

    /*
     * 실제 Diamond 크기.
     *
     * FAR    -> 3 x 3
     * MEDIUM -> 4 x 4
     * NEAR   -> 5 x 5
     *
     * 가까운 플레이어일수록 크게 표시한다.
     *
     * 실제 거리 threshold는 player position을
     * 연결할 때 결정한다.
     */
    public enum PlayerMarkerSize {
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

    private CompassHud() {
    }

    // ------------------------------------------------------------
    // GUI Layer Registration
    // ------------------------------------------------------------

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                COMPASS_HUD,
                CompassHud::render
        );
    }

    // ------------------------------------------------------------
    // Render
    // ------------------------------------------------------------

    private static void render(
            GuiGraphicsExtractor graphics,
            net.minecraft.client.DeltaTracker deltaTracker
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        int centerX =
                graphics.guiWidth() / 2;

        // ------------------------------------------------------------
        // Camera Heading
        // ------------------------------------------------------------

        Camera camera =
                minecraft.gameRenderer.getMainCamera();

        /*
         * Minecraft yaw:
         *
         * South =   0°
         * West  =  90°
         * North = 180°
         * East  = -90°
         *
         * Compass heading:
         *
         * North =   0°
         * East  =  90°
         * South = 180°
         * West  = 270°
         */
        float heading =
                normalizeDegrees(
                        camera.yRot() + 180.0F
                );

        // ------------------------------------------------------------
        // Compass
        // ------------------------------------------------------------

        drawCompass(
                graphics,
                minecraft,
                centerX,
                heading
        );

        // ------------------------------------------------------------
        // Fixed Center Indicator
        // ------------------------------------------------------------

        drawTick(
                graphics,
                centerX,
                TICK_BOTTOM_Y,
                2,
                CENTER_TICK_HEIGHT,
                CENTER_TICK_COLOR
        );

        /*
         * Player marker는 아직 렌더링하지 않는다.
         *
         * 다음 단계에서 Server -> Client network를 연결한 뒤
         * 실제 player position을 기반으로 drawPlayerMarker()를
         * 호출한다.
         */
    }

    // ------------------------------------------------------------
    // Compass
    // ------------------------------------------------------------

    private static void drawCompass(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            int centerX,
            float heading
    ) {
        int baseTick =
                (int) Math.floor(
                        heading / DEGREES_PER_TICK
                );

        int halfWidth =
                HALF_TICKS * TICK_SPACING;

        /*
         * 좌우 끝에서 빈 공간이 생기지 않도록
         * 한 tick씩 추가로 계산한다.
         */
        for (
                int offset = -HALF_TICKS - 1;
                offset <= HALF_TICKS + 1;
                offset++
        ) {
            int tickIndex =
                    baseTick + offset;

            float tickAngle =
                    tickIndex * DEGREES_PER_TICK;

            /*
             * 현재 Camera 기준 상대 각도.
             *
             * negative -> 화면 왼쪽
             * positive -> 화면 오른쪽
             */
            float relativeAngle =
                    Mth.wrapDegrees(
                            tickAngle - heading
                    );

            /*
             * Angle -> Screen pixel
             */
            float screenOffset =
                    relativeAngle
                            / DEGREES_PER_TICK
                            * TICK_SPACING;

            int x =
                    Math.round(
                            centerX + screenOffset
                    );

            int distanceFromCenter =
                    Math.abs(
                            x - centerX
                    );

            if (distanceFromCenter > halfWidth) {
                continue;
            }

            /*
             * 360 / 11.25 = 32 ticks
             */
            int normalizedTick =
                    Math.floorMod(
                            tickIndex,
                            32
                    );

            // --------------------------------------------------------
            // Edge Scale
            // --------------------------------------------------------

            /*
             * center = 0.0
             * edge   = 1.0
             */
            float edgeRatio =
                    Math.min(
                            1.0F,
                            distanceFromCenter
                                    / (float) halfWidth
                    );

            // --------------------------------------------------------
            // Tick Style
            // --------------------------------------------------------

            int tickHeight;
            int tickWidth;
            int tickColor;

            if (normalizedTick % 4 == 0) {

                /*
                 * Major tick
                 *
                 * center -> 6px
                 * edge   -> 4px
                 */
                tickHeight =
                        Math.max(
                                4,
                                Math.round(
                                        6.0F
                                                - 2.0F
                                                * edgeRatio
                                )
                        );

                tickWidth = 1;
                tickColor = MAJOR_TICK_COLOR;

            } else if (normalizedTick % 2 == 0) {

                /*
                 * Medium tick
                 *
                 * center -> 4px
                 * edge   -> 3px
                 */
                tickHeight =
                        Math.max(
                                3,
                                Math.round(
                                        4.0F
                                                - edgeRatio
                                )
                        );

                tickWidth = 1;
                tickColor = MEDIUM_TICK_COLOR;

            } else {

                /*
                 * Minor tick은 항상 3px.
                 */
                tickHeight = 3;
                tickWidth = 1;
                tickColor = MINOR_TICK_COLOR;
            }

            drawTick(
                    graphics,
                    x,
                    TICK_BOTTOM_Y,
                    tickWidth,
                    tickHeight,
                    tickColor
            );

            // --------------------------------------------------------
            // Direction Label
            // --------------------------------------------------------

            if (
                    normalizedTick % 4 == 0
                            && Math.abs(relativeAngle)
                            <= LABEL_VISIBLE_ANGLE
            ) {
                String label =
                        getDirectionLabel(
                                normalizedTick
                        );

                int labelColor =
                        getDirectionLabelColor(
                                label
                        );

                drawLabel(
                        graphics,
                        minecraft,
                        label,
                        x,
                        LABEL_Y,
                        labelColor
                );
            }
        }
    }

    // ------------------------------------------------------------
    // Player Marker
    // ------------------------------------------------------------

    /*
     * 실제 player data를 연결한 이후 사용한다.
     *
     * playerHeading:
     *
     * North =   0°
     * East  =  90°
     * South = 180°
     * West  = 270°
     */
    public static void drawPlayerMarker(
            GuiGraphicsExtractor graphics,
            int centerX,
            float cameraHeading,
            float playerHeading,
            PlayerMarkerSize markerSize
    ) {
        /*
         * World-space player heading을
         * Camera 기준 상대 각도로 변환한다.
         */
        float relativeAngle =
                Mth.wrapDegrees(
                        playerHeading
                                - cameraHeading
                );

        /*
         * 현재 Compass Bar가 표현하는 최대 각도.
         */
        float maxVisibleAngle =
                HALF_TICKS
                        * DEGREES_PER_TICK;

        if (Math.abs(relativeAngle) > maxVisibleAngle) {
            return;
        }

        /*
         * Angle -> Screen pixel
         */
        float screenOffset =
                relativeAngle
                        / DEGREES_PER_TICK
                        * TICK_SPACING;

        int x =
                Math.round(
                        centerX + screenOffset
                );

        drawDiamondMarker(
                graphics,
                x,
                MARKER_BOTTOM_Y,
                markerSize.size()
        );
    }

    // ------------------------------------------------------------
    // Diamond Marker
    // ------------------------------------------------------------

    /*
     * 3 x 3
     *
     *  █
     * ███
     *  █
     *
     *
     * 4 x 4
     *
     *  ██
     * ████
     * ████
     *  ██
     *
     *
     * 5 x 5
     *
     *   █
     *  ███
     * █████
     *  ███
     *   █
     *
     *
     * 모든 marker는 MARKER_BOTTOM_Y를 기준으로
     * 하단 정렬한다.
     */
    private static void drawDiamondMarker(
            GuiGraphicsExtractor graphics,
            int centerX,
            int bottomY,
            int size
    ) {
        int topY =
                bottomY - size + 1;

        for (
                int row = 0;
                row < size;
                row++
        ) {
            int rowWidth;

            if (size % 2 == 1) {

                /*
                 * Odd
                 *
                 * 3 -> 1, 3, 1
                 * 5 -> 1, 3, 5, 3, 1
                 */
                int centerRow =
                        size / 2;

                rowWidth =
                        size
                                - 2
                                * Math.abs(
                                row - centerRow
                        );

            } else {

                /*
                 * Even
                 *
                 * 4 -> 2, 4, 4, 2
                 */
                int distanceFromEdge =
                        Math.min(
                                row,
                                size - 1 - row
                        );

                rowWidth =
                        2
                                * (
                                distanceFromEdge + 1
                        );
            }

            int left =
                    centerX
                            - rowWidth / 2;

            int y =
                    topY + row;

            graphics.fill(
                    left,
                    y,
                    left + rowWidth,
                    y + 1,
                    PLAYER_MARKER_COLOR
            );
        }
    }

    // ------------------------------------------------------------
    // Direction Labels
    // ------------------------------------------------------------

    private static String getDirectionLabel(
            int normalizedTick
    ) {
        int directionIndex =
                normalizedTick / 4;

        return switch (directionIndex) {
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

    private static int getDirectionLabelColor(
            String label
    ) {
        return switch (label) {

            // X Axis
            case "E", "W" ->
                    X_AXIS_TEXT_COLOR;

            // Z Axis
            case "N", "S" ->
                    Z_AXIS_TEXT_COLOR;

            // Diagonal
            default ->
                    WHITE_TEXT_COLOR;
        };
    }

    // ------------------------------------------------------------
    // Drawing Helpers
    // ------------------------------------------------------------

    private static void drawTick(
            GuiGraphicsExtractor graphics,
            int centerX,
            int bottomY,
            int width,
            int height,
            int color
    ) {
        int left =
                centerX - width / 2;

        graphics.fill(
                left,
                bottomY - height,
                left + width,
                bottomY,
                color
        );
    }

    private static void drawLabel(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            String text,
            int x,
            int y,
            int color
    ) {
        graphics.centeredText(
                minecraft.font,
                Component.literal(text),
                x,
                y,
                color
        );
    }

    // ------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------

    private static float normalizeDegrees(
            float degrees
    ) {
        degrees %= 360.0F;

        if (degrees < 0.0F) {
            degrees += 360.0F;
        }

        return degrees;
    }
}