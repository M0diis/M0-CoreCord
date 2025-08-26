package me.m0dii.corecord.listeners;

import me.m0dii.corecord.CoreCord;
import me.m0dii.corecord.utils.Config;
import me.m0dii.corecord.utils.EActionType;
import me.m0dii.corecord.utils.WebhookLogger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class LoggerListener implements Listener {
    private final Config cfg;

    public LoggerListener(@NotNull CoreCord plugin) {
        this.cfg = plugin.getCfg();
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.BLOCK_PLACE);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        hook.send(name, "Placed a block at " + "X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ()
                + " (" + block.getType().name() + ")");
    }

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.CHAT_MESSAGE);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        String message = event.getMessage();

        hook.send(name, "Sent a message: " + message);
    }

    @EventHandler
    public void onCommand(final PlayerCommandPreprocessEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.COMMAND_EXECUTION);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        String message = event.getMessage();

        hook.send(name, "Executed a command: " + message);
    }

    private static final List<Material> containers = Arrays.asList(Material.CHEST, Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.FURNACE, Material.BREWING_STAND, Material.CAULDRON, Material.ENDER_CHEST);

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.ITEM_DROP);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        ItemStack itemStack = event.getItemDrop().getItemStack();

        Location loc = event.getItemDrop().getLocation();

        hook.send(name, "Dropped an item at X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ()
                + " (" + itemStack.getType() + " x" + itemStack.getAmount() + ")");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(final PlayerAttemptPickupItemEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.ITEM_PICKUP);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        ItemStack itemStack = event.getItem().getItemStack();

        Location loc = event.getItem().getLocation();

        hook.send(name, "Picked up an item at X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ()
                + " (" + itemStack.getType() + " x" + itemStack.getAmount() + ")");
    }

    @EventHandler(ignoreCancelled = true)
    public void onContainerOpen(final PlayerInteractEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.CONTAINER_OPEN);

        if (hook == null) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        String name = event.getPlayer().getName();

        Block clicked = event.getClickedBlock();

        if (clicked == null)
            return;

        if (!containers.contains(clicked.getType()))
            return;

        Location loc = clicked.getLocation();

        hook.send(name, "Opened a container at X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ() + ". (" + clicked.getType().name() + ")");
    }

    @EventHandler
    public void onMobKill(final EntityDeathEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.KILL);

        if (hook == null) {
            return;
        }

        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        String name = killer.getName();
        String mob = event.getEntity().getType().name();

        Location loc = event.getEntity().getLocation();

        hook.send(name, "Killed a " + mob + " at X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.BLOCK_BREAK);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        hook.send(name, "Broke a block at " + "X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ()
                + " (" + block.getType().name() + ")");
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.PLAYER_JOIN);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();

        Location loc = event.getPlayer().getLocation();

        hook.send(name, "Joined the server. (X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ() + ")");
    }

    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent event) {
        WebhookLogger hook = cfg.getWebhook(EActionType.PLAYER_QUIT);

        if (hook == null) {
            return;
        }

        String name = event.getPlayer().getName();
        Location loc = event.getPlayer().getLocation();

        hook.send(name, "Left the server. (X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ() + ")");
    }

}
