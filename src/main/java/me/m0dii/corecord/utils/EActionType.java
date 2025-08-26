package me.m0dii.corecord.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Getter
public enum EActionType {

    BLOCK_PLACE("+block", "place"),
    BLOCK_BREAK("-block", "break"),
    ITEM_PICKUP("+item", "pickup", "pickupitem"),
    ITEM_DROP("-item", "drop", "dropitem"),
    PLAYER_JOIN("+session", "join", "playerjoin"),
    PLAYER_QUIT("-session", "quit", "playerquit"),
    CONTAINER_OPEN("container", "open"),
    CHAT_MESSAGE("chat", "message", "send"),
    COMMAND_EXECUTION("command", "execute", "exec"),
    KILL("kill", "mobkill", "monsterkill");

    private final String[] aliases;

    EActionType(String... aliases) {
        this.aliases = aliases;
    }

    @Nullable
    public static EActionType fromString(@NotNull String action) {
        return Arrays.stream(values())
                .filter(type -> Arrays.stream(type.getAliases())
                        .anyMatch(alias -> alias.equalsIgnoreCase(action)))
                .findFirst()
                .orElse(null);
    }
}
