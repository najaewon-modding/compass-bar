package net.njw.compassbar.server;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.njw.compassbar.CompassBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PlayerColorSavedData extends SavedData {
    private static final int RGB_MASK = 0x00FFFFFF;
    private static final int RGB_SPACE_SIZE = 1 << 24;
    private static final int CANDIDATE_COUNT = 768;
    private static final double MIN_HSL_SATURATION = 0.68;
    private static final double MAX_HSL_SATURATION = 0.96;
    private static final double MIN_HSL_LIGHTNESS = 0.52;
    private static final double MAX_HSL_LIGHTNESS = 0.70;
    private static final double MIN_OKLAB_LIGHTNESS = 0.62;
    private static final double MAX_OKLAB_LIGHTNESS = 0.84;
    private static final double MIN_OKLAB_CHROMA = 0.10;

    private static final Codec<Map<String, Integer>> COLOR_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT);

    public static final Codec<PlayerColorSavedData> CODEC = COLOR_MAP_CODEC.xmap(
            PlayerColorSavedData::fromSerialized,
            PlayerColorSavedData::toSerialized
    );

    public static final SavedDataType<PlayerColorSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(CompassBar.MODID, "player_colors"),
            PlayerColorSavedData::new,
            CODEC,
            null
    );

    private final Map<UUID, Integer> colors;
    private final Set<Integer> usedColors;
    private final List<OklabColor> usedOklabColors;

    public PlayerColorSavedData() {
        this(Map.of());
    }

    private PlayerColorSavedData(Map<UUID, Integer> initialColors) {
        this.colors = new HashMap<>(initialColors);
        this.usedColors = new HashSet<>(initialColors.values());
        this.usedOklabColors = new ArrayList<>(initialColors.size());

        for (int rgb : initialColors.values()) {
            this.usedOklabColors.add(toOklab(rgb));
        }
    }

    public static PlayerColorSavedData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean ensureAssigned(List<ServerPlayer> players) {
        boolean changed = false;

        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            if (colors.containsKey(uuid)) continue;

            assignNewColor(uuid);
            changed = true;
        }

        if (changed) setDirty();
        return changed;
    }

    public int getOrAssignColor(UUID uuid) {
        Integer existing = colors.get(uuid);
        if (existing != null) return existing;

        int color = assignNewColor(uuid);
        setDirty();
        return color;
    }

    private int assignNewColor(UUID uuid) {
        int color = generateDistinctVisibleColor();
        colors.put(uuid, color);
        usedColors.add(color);
        usedOklabColors.add(toOklab(color));
        return color;
    }

    private int generateDistinctVisibleColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (usedColors.isEmpty()) return randomVisibleColor(random);

        int bestColor = -1;
        double bestMinimumDistanceSquared = -1.0;

        for (int candidateIndex = 0; candidateIndex < CANDIDATE_COUNT; candidateIndex++) {
            int candidate = randomVisibleColor(random);
            if (usedColors.contains(candidate)) continue;

            OklabColor candidateLab = toOklab(candidate);
            double minimumDistanceSquared = Double.POSITIVE_INFINITY;

            for (OklabColor existing : usedOklabColors) {
                double distanceSquared = candidateLab.distanceSquared(existing);
                if (distanceSquared < minimumDistanceSquared) minimumDistanceSquared = distanceSquared;
                if (minimumDistanceSquared <= bestMinimumDistanceSquared) break;
            }

            if (minimumDistanceSquared > bestMinimumDistanceSquared) {
                bestMinimumDistanceSquared = minimumDistanceSquared;
                bestColor = candidate;
            }
        }

        if (bestColor >= 0) return bestColor;
        return findUniqueVisibleColor(random);
    }

    private int findUniqueVisibleColor(ThreadLocalRandom random) {
        int start = random.nextInt(RGB_SPACE_SIZE);

        for (int offset = 0; offset < RGB_SPACE_SIZE; offset++) {
            int candidate = (start + offset) & RGB_MASK;
            if (!usedColors.contains(candidate) && isVisible(candidate)) return candidate;
        }

        throw new IllegalStateException("No distinct visible player colors remain");
    }

    private static int randomVisibleColor(ThreadLocalRandom random) {
        while (true) {
            double hue = random.nextDouble();
            double saturation = random.nextDouble(MIN_HSL_SATURATION, MAX_HSL_SATURATION);
            double lightness = random.nextDouble(MIN_HSL_LIGHTNESS, MAX_HSL_LIGHTNESS);
            int rgb = hslToRgb(hue, saturation, lightness);
            if (isVisible(rgb)) return rgb;
        }
    }

    private static boolean isVisible(int rgb) {
        OklabColor lab = toOklab(rgb);
        double chroma = Math.hypot(lab.a(), lab.b());
        return lab.l() >= MIN_OKLAB_LIGHTNESS
                && lab.l() <= MAX_OKLAB_LIGHTNESS
                && chroma >= MIN_OKLAB_CHROMA;
    }

    private static int hslToRgb(double hue, double saturation, double lightness) {
        double chroma = (1.0 - Math.abs(2.0 * lightness - 1.0)) * saturation;
        double huePrime = hue * 6.0;
        double x = chroma * (1.0 - Math.abs(huePrime % 2.0 - 1.0));

        double r1;
        double g1;
        double b1;

        if (huePrime < 1.0) {
            r1 = chroma;
            g1 = x;
            b1 = 0.0;
        } else if (huePrime < 2.0) {
            r1 = x;
            g1 = chroma;
            b1 = 0.0;
        } else if (huePrime < 3.0) {
            r1 = 0.0;
            g1 = chroma;
            b1 = x;
        } else if (huePrime < 4.0) {
            r1 = 0.0;
            g1 = x;
            b1 = chroma;
        } else if (huePrime < 5.0) {
            r1 = x;
            g1 = 0.0;
            b1 = chroma;
        } else {
            r1 = chroma;
            g1 = 0.0;
            b1 = x;
        }

        double m = lightness - chroma / 2.0;
        int red = clampChannel((int) Math.round((r1 + m) * 255.0));
        int green = clampChannel((int) Math.round((g1 + m) * 255.0));
        int blue = clampChannel((int) Math.round((b1 + m) * 255.0));
        return red << 16 | green << 8 | blue;
    }

    private static int clampChannel(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static OklabColor toOklab(int rgb) {
        double red = srgbToLinear((rgb >> 16 & 0xFF) / 255.0);
        double green = srgbToLinear((rgb >> 8 & 0xFF) / 255.0);
        double blue = srgbToLinear((rgb & 0xFF) / 255.0);

        double l = 0.4122214708 * red + 0.5363325363 * green + 0.0514459929 * blue;
        double m = 0.2119034982 * red + 0.6806995451 * green + 0.1073969566 * blue;
        double s = 0.0883024619 * red + 0.2817188376 * green + 0.6299787005 * blue;

        double lRoot = Math.cbrt(l);
        double mRoot = Math.cbrt(m);
        double sRoot = Math.cbrt(s);

        return new OklabColor(
                0.2104542553 * lRoot + 0.7936177850 * mRoot - 0.0040720468 * sRoot,
                1.9779984951 * lRoot - 2.4285922050 * mRoot + 0.4505937099 * sRoot,
                0.0259040371 * lRoot + 0.7827717662 * mRoot - 0.8086757660 * sRoot
        );
    }

    private static double srgbToLinear(double value) {
        return value <= 0.04045
                ? value / 12.92
                : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    private static PlayerColorSavedData fromSerialized(Map<String, Integer> serialized) {
        Map<UUID, Integer> decoded = new HashMap<>();
        Set<Integer> seenColors = new HashSet<>();

        for (Map.Entry<String, Integer> entry : serialized.entrySet()) {
            try {
                UUID uuid = UUID.fromString(entry.getKey());
                int rgb = entry.getValue() & RGB_MASK;
                if (seenColors.add(rgb)) decoded.put(uuid, rgb);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new PlayerColorSavedData(decoded);
    }

    private Map<String, Integer> toSerialized() {
        Map<String, Integer> serialized = new HashMap<>(colors.size());
        colors.forEach((uuid, rgb) -> serialized.put(uuid.toString(), rgb));
        return serialized;
    }

    private record OklabColor(double l, double a, double b) {
        private double distanceSquared(OklabColor other) {
            double dl = l - other.l;
            double da = a - other.a;
            double db = b - other.b;
            return dl * dl + da * da + db * db;
        }
    }
}
