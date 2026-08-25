package net.njw.compassbar.client;

import net.njw.compassbar.network.PlayerPositionData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PlayerColorManager {

    private static final Map<UUID, Integer> PLAYER_COLORS =
            new HashMap<>();

    /*
     * HUD에서 잘 보이는 밝은 색들.
     *
     * Alpha는 모두 FF.
     */
    private static final int[] COLOR_PALETTE = {
            0xFFFF6B6B, // Red
            0xFFFF9F43, // Orange
            0xFFFFD93D, // Yellow
            0xFF6BCB77, // Green
            0xFF4DDFB8, // Mint
            0xFF4FC3F7, // Light Blue
            0xFF5C7CFA, // Blue
            0xFF9B72FF, // Purple
            0xFFD66EFA, // Violet
            0xFFFF7EB6  // Pink
    };

    private PlayerColorManager() {
    }

    /*
     * HUD를 열 때 현재 접속 중인 플레이어들에게
     * 아직 색이 없다면 새 색을 배정한다.
     */
    public static void assignMissingColors(
            List<PlayerPositionData> players,
            UUID localPlayerUuid
    ) {
        for (PlayerPositionData player : players) {

            if (player.uuid().equals(localPlayerUuid)) {
                continue;
            }

            PLAYER_COLORS.computeIfAbsent(
                    player.uuid(),
                    uuid -> createRandomColor()
            );
        }
    }

    /*
     * HUD가 열린 상태에서 새로운 플레이어가 접속한 경우를 위해
     * 렌더링 시에도 안전하게 색을 가져올 수 있도록 한다.
     */
    public static int getOrAssignColor(UUID uuid) {
        return PLAYER_COLORS.computeIfAbsent(
                uuid,
                ignored -> createRandomColor()
        );
    }

    private static int createRandomColor() {
        int index =
                ThreadLocalRandom.current().nextInt(
                        COLOR_PALETTE.length
                );

        return COLOR_PALETTE[index];
    }
}