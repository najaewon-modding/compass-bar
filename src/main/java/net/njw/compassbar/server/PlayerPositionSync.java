package net.njw.compassbar.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.njw.compassbar.CompassBar;
import net.njw.compassbar.network.CompassSubscriptionPayload;
import net.njw.compassbar.network.PlayerPositionData;
import net.njw.compassbar.network.PlayerPositionsPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CompassBar.MODID)
public final class PlayerPositionSync {
    private static final int SYNC_INTERVAL_TICKS = 1;
    private static final Set<UUID> ACTIVE_PLAYERS = new HashSet<>();
    private static int tickCounter = 0;

    private PlayerPositionSync() {}

    public static void setCompassActive(ServerPlayer player, boolean active) {
        UUID uuid = player.getUUID();

        if (active) {
            ACTIVE_PLAYERS.add(uuid);
            sendCurrentPositions(player);
        } else {
            ACTIVE_PLAYERS.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < SYNC_INTERVAL_TICKS) return;
        tickCounter = 0;

        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();

        if (players.isEmpty()) {
            ACTIVE_PLAYERS.clear();
            return;
        }

        Set<UUID> onlinePlayerIds =
                players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());
        ACTIVE_PLAYERS.retainAll(onlinePlayerIds);

        List<ServerPlayer> recipients =
                players.stream().filter(PlayerPositionSync::shouldReceivePositions).toList();
        if (recipients.isEmpty()) return;

        Map<ResourceKey<Level>, List<PlayerPositionData>> positionsByDimension = players.stream()
                .collect(Collectors.groupingBy(
                        player -> player.level().dimension(),
                        Collectors.mapping(PlayerPositionSync::createPositionData, Collectors.toList())
                ));

        Map<ResourceKey<Level>, PlayerPositionsPayload> payloadsByDimension = new HashMap<>();

        for (ServerPlayer recipient : recipients) {
            ResourceKey<Level> dimension = recipient.level().dimension();
            PlayerPositionsPayload payload = payloadsByDimension.computeIfAbsent(
                    dimension,
                    key -> new PlayerPositionsPayload(positionsByDimension.getOrDefault(key, List.of()))
            );
            PacketDistributor.sendToPlayer(recipient, payload);
        }
    }

    private static boolean shouldReceivePositions(ServerPlayer player) {
        if (!player.connection.hasChannel(PlayerPositionsPayload.TYPE)) return false;
        if (!player.connection.hasChannel(CompassSubscriptionPayload.TYPE)) return true;
        return ACTIVE_PLAYERS.contains(player.getUUID());
    }

    private static void sendCurrentPositions(ServerPlayer recipient) {
        if (!recipient.connection.hasChannel(PlayerPositionsPayload.TYPE)) return;

        ResourceKey<Level> dimension = recipient.level().dimension();
        List<PlayerPositionData> positions = recipient.level().getServer().getPlayerList().getPlayers().stream()
                .filter(player -> player.level().dimension().equals(dimension))
                .map(PlayerPositionSync::createPositionData)
                .toList();

        PacketDistributor.sendToPlayer(recipient, new PlayerPositionsPayload(positions));
    }

    private static PlayerPositionData createPositionData(ServerPlayer player) {
        return new PlayerPositionData(
                player.getUUID(),
                player.level().dimension(),
                player.getX(),
                player.getZ()
        );
    }
}