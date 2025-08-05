package me.m0dii.corecord.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.m0dii.corecord.CoreCord;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    private final CoreCord plugin;

    public Placeholders(CoreCord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "m0dii";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "corecord";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String id) {
        return "";
    }
}