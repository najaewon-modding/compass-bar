package net.njw.compassbar.client;

public final class CompassState {

    private static boolean visible = false;

    private CompassState() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void toggle() {
        visible = !visible;
    }

    public static void hide() {
        visible = false;
    }
}