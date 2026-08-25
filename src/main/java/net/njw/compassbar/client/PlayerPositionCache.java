package net.njw.compassbar.client;

import net.njw.compassbar.network.PlayerPositionData;

import java.util.List;

public final class PlayerPositionCache {

    private static volatile List<PlayerPositionData> players =
            List.of();

    private PlayerPositionCache() {
    }

    public static void update(
            List<PlayerPositionData> newPlayers
    ) {
        players = List.copyOf(newPlayers);
    }

    public static List<PlayerPositionData> getPlayers() {
        return players;
    }

    public static void clear() {
        players = List.of();
    }
}