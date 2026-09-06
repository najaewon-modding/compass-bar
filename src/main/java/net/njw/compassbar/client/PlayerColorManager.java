package net.njw.compassbar.client;

import net.njw.compassbar.network.PlayerColorData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerColorManager {
    private static final int FALLBACK_COLOR = 0xFFFFFFFF;
    private static volatile Map<UUID, Integer> playerColors = Map.of();

    private PlayerColorManager() {}

    public static void update(List<PlayerColorData> colors) {
        Map<UUID, Integer> nextColors = new HashMap<>(Math.max(16, colors.size() * 2));

        for (PlayerColorData color : colors) {
            nextColors.put(color.uuid(), 0xFF000000 | color.rgb() & 0x00FFFFFF);
        }

        playerColors = Map.copyOf(nextColors);
    }

    public static int getOrAssignColor(UUID uuid) {
        return playerColors.getOrDefault(uuid, FALLBACK_COLOR);
    }

    public static void clear() {
        playerColors = Map.of();
    }
}
