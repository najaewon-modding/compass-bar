package net.njw.compassbar.server;

import net.njw.compassbar.CompassBar;
import net.njw.compassbar.network.PlayerPositionData;
import net.njw.compassbar.network.PlayerPositionsPayload;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(
        modid = CompassBar.MODID
)
public final class PlayerPositionSync {

    /*
     * Minecraft = 20 ticks / second
     *
     * 2 ticks마다 전송:
     * 10 updates / second
     */
    private static final int SYNC_INTERVAL_TICKS = 2;

    private static int tickCounter = 0;

    private PlayerPositionSync() {
    }

    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event
    ) {
        tickCounter++;

        if (tickCounter < SYNC_INTERVAL_TICKS) {
            return;
        }

        tickCounter = 0;

        List<ServerPlayer> players =
                event.getServer()
                        .getPlayerList()
                        .getPlayers();

        /*
         * 현재 접속 중인 모든 플레이어의
         * 위치 snapshot을 한 번 생성한다.
         */
        List<PlayerPositionData> positions =
                players.stream()
                        .map(player ->
                                new PlayerPositionData(
                                        player.getUUID(),
                                        player.level().dimension(),
                                        player.getX(),
                                        player.getZ()
                                )
                        )
                        .toList();

        PlayerPositionsPayload payload =
                new PlayerPositionsPayload(positions);

        /*
         * 이 payload를 지원하는 client에게만 보낸다.
         *
         * 모드가 없는 client에는 전송하지 않는다.
         */
        for (ServerPlayer recipient : players) {

            if (!recipient.connection.hasChannel(
                    PlayerPositionsPayload.TYPE
            )) {
                continue;
            }

            PacketDistributor.sendToPlayer(
                    recipient,
                    payload
            );
        }
    }
}