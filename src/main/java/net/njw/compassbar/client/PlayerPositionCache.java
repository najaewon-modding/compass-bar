package net.njw.compassbar.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.njw.compassbar.network.PlayerPositionData;

public final class PlayerPositionCache {
    private static final long DEFAULT_INTERPOLATION_NANOS = 50_000_000L;
    private static final long MIN_INTERPOLATION_NANOS = 25_000_000L;
    private static final long MAX_INTERPOLATION_NANOS = 100_000_000L;
    private static final double TELEPORT_SNAP_DISTANCE = 32.0;
    private static final double TELEPORT_SNAP_DISTANCE_SQUARED =
            TELEPORT_SNAP_DISTANCE * TELEPORT_SNAP_DISTANCE;

    private static volatile List<PlayerPositionData> players = List.of();
    private static volatile Map<UUID, InterpolatedPosition> interpolatedPositions = Map.of();
    private static long lastUpdateNanos = 0L;

    private PlayerPositionCache() {}

    public static synchronized void update(List<PlayerPositionData> newPlayers) {
        long now = System.nanoTime();
        long interpolationDuration = calculateInterpolationDuration(now);
        Map<UUID, InterpolatedPosition> previousPositions = interpolatedPositions;
        Map<UUID, InterpolatedPosition> nextPositions =
                new HashMap<>(Math.max(16, newPlayers.size() * 2));

        for (PlayerPositionData player : newPlayers) {
            InterpolatedPosition previous = previousPositions.get(player.uuid());
            double startX;
            double startZ;

            if (previous != null && previous.dimension().equals(player.dimension())) {
                startX = previous.x(now);
                startZ = previous.z(now);
            } else {
                startX = player.x();
                startZ = player.z();
            }

            double movementX = player.x() - startX;
            double movementZ = player.z() - startZ;
            double movementSquared = movementX * movementX + movementZ * movementZ;

            if (movementSquared >= TELEPORT_SNAP_DISTANCE_SQUARED) {
                startX = player.x();
                startZ = player.z();
            }

            nextPositions.put(
                    player.uuid(),
                    new InterpolatedPosition(
                            player.dimension(),
                            startX,
                            startZ,
                            player.x(),
                            player.z(),
                            now,
                            interpolationDuration
                    )
            );
        }

        players = List.copyOf(newPlayers);
        interpolatedPositions = Map.copyOf(nextPositions);
        lastUpdateNanos = now;
    }

    public static List<PlayerPositionData> getPlayers() {
        return players;
    }

    public static InterpolatedPosition getInterpolatedPosition(UUID uuid) {
        return interpolatedPositions.get(uuid);
    }

    public static synchronized void clear() {
        players = List.of();
        interpolatedPositions = Map.of();
        lastUpdateNanos = 0L;
    }

    private static long calculateInterpolationDuration(long now) {
        if (lastUpdateNanos == 0L) return DEFAULT_INTERPOLATION_NANOS;

        long packetInterval = now - lastUpdateNanos;
        return Math.max(MIN_INTERPOLATION_NANOS, Math.min(MAX_INTERPOLATION_NANOS, packetInterval));
    }

    public static final class InterpolatedPosition {
        private final ResourceKey<Level> dimension;
        private final double startX;
        private final double startZ;
        private final double targetX;
        private final double targetZ;
        private final long startNanos;
        private final long durationNanos;

        private InterpolatedPosition(
                ResourceKey<Level> dimension,
                double startX,
                double startZ,
                double targetX,
                double targetZ,
                long startNanos,
                long durationNanos
        ) {
            this.dimension = dimension;
            this.startX = startX;
            this.startZ = startZ;
            this.targetX = targetX;
            this.targetZ = targetZ;
            this.startNanos = startNanos;
            this.durationNanos = durationNanos;
        }

        public ResourceKey<Level> dimension() {
            return dimension;
        }

        public double x(long nowNanos) {
            double progress = interpolationProgress(nowNanos);
            return startX + (targetX - startX) * progress;
        }

        public double z(long nowNanos) {
            double progress = interpolationProgress(nowNanos);
            return startZ + (targetZ - startZ) * progress;
        }

        private double interpolationProgress(long nowNanos) {
            if (durationNanos <= 0L) return 1.0;

            double progress = (nowNanos - startNanos) / (double) durationNanos;

            if (progress <= 0.0) return 0.0;
            if (progress >= 1.0) return 1.0;
            return progress;
        }
    }
}