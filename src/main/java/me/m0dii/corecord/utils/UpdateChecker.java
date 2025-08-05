package me.m0dii.corecord.utils;

import me.m0dii.corecord.CoreCord;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final CoreCord plugin;
    private final int resourceId;

    public UpdateChecker(@NotNull CoreCord plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
        {
            try (InputStream inputStream = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)
                    .toURL()
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext())
                    consumer.accept(scanner.next());
            } catch (IOException ex) {
                Messenger.warn("Failed to check for updates.");
                Messenger.debug(ex.getMessage());
            }
        });
    }
}