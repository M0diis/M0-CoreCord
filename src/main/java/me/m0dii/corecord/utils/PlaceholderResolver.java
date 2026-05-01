package me.m0dii.corecord.utils;

import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Uses PlaceholderAPI through reflection so this plugin can still start when PlaceholderAPI is absent.
 */
public final class PlaceholderResolver {
    private static boolean initialized;
    private static boolean available;
    private static Method setPlaceholders;

    private PlaceholderResolver() {
    }

    public static String apply(@NotNull String text, PluginManager pluginManager) {
        if (text.isEmpty()) {
            return text;
        }

        if (!initialized) {
            initialize(pluginManager);
        }

        if (!available || setPlaceholders == null) {
            return text;
        }

        try {
            Object result = setPlaceholders.invoke(null, null, text);
            return result instanceof String resolved ? resolved : text;
        } catch (Exception ex) {
            Messenger.debug("Failed to resolve PlaceholderAPI placeholders: " + ex.getMessage());
            return text;
        }
    }

    private static void initialize(PluginManager pluginManager) {
        initialized = true;

        if (pluginManager.getPlugin("PlaceholderAPI") == null) {
            available = false;
            return;
        }

        try {
            Class<?> placeholderApiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            setPlaceholders = placeholderApiClass.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class);
            available = true;
        } catch (Exception ex) {
            available = false;
            Messenger.debug("PlaceholderAPI detected but could not be linked: " + ex.getMessage());
        }
    }
}

