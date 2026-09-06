package net.njw.compassbar.client;

public final class CompassState {

    private static boolean visible = true;

    private CompassState() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void show() {
        visible = true;
    }

    public static void toggle() {
        visible = !visible;
    }

    public static void hide() {
        visible = false;
    }
}