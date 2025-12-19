package de.donutsmp.spawner.util;

public class TextUtil {

    public static String formatTitle(String text) {
        return "§5§l" + text;
    }

    public static String formatSuccess(String text) {
        return "§a✓ " + text;
    }

    public static String formatError(String text) {
        return "§c✗ " + text;
    }

    public static String formatInfo(String label, String value) {
        return "§7" + label + ": §e" + value;
    }

    public static String formatButton(String text) {
        return text;
    }
}
