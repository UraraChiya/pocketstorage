package com.flanks255.psu.commands;

import com.flanks255.psu.inventory.StorageManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class List {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("list").requires(cs -> cs.hasPermission(1)).executes(List::list)
            .then(Commands.literal("firstOpenedBy").then(Commands.argument("PlayerName", StringArgumentType.string())
                .suggests((cs, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(cs), builder))
                .executes(cs -> first(cs, StringArgumentType.getString(cs, "PlayerName")))))
            .then(Commands.literal("lastOpenedBy").then(Commands.argument("PlayerName", StringArgumentType.string())
                .suggests((cs, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(cs), builder))
                .executes(cs -> last(cs, StringArgumentType.getString(cs, "PlayerName")))));
    }

    public static Set<String> getPlayerSuggestions(CommandContext<CommandSourceStack> commandSource) {
        Set<String> list = new HashSet<>();
        commandSource.getSource().getServer().getPlayerList().getPlayers().forEach( serverPlayerEntity -> list.add(serverPlayerEntity.getName().getString()));

        return list;
    }

    public static int list(CommandContext<CommandSourceStack> ctx) {
        StorageManager storageManager = StorageManager.get();

        if (storageManager.getMap().size() == 0) {
            ctx.getSource().sendSuccess(Component.literal("[ ]"), false);
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        storageManager.getMap().forEach( (uuid, psuData) -> ctx.getSource().sendSuccess(Component.literal(
            "Truncated-UUID: " + psuData.getUuid().toString().substring(0,8) + "\nCreated By: " + psuData.meta.getLastAccessedPlayer() + " On: " + sdf.format(new Date(psuData.meta.getFirstAccessedTime())) + "\nLast accessed by: " + psuData.meta.getLastAccessedPlayer() + " on: " + sdf.format(new Date(psuData.meta.getLastAccessedTime()))
        ), false));

        return 0;
    }

    public static int first(CommandContext<CommandSourceStack> ctx, String playerName) {
        StorageManager storageManager = StorageManager.get();

        if (storageManager.getMap().size() == 0) {
            ctx.getSource().sendSuccess(Component.literal("[ ]"), false);
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        storageManager.getMap().forEach( (uuid, psuData) -> {
            if (psuData.meta.getFirstAccessedPlayer().equalsIgnoreCase(playerName)) {
                ctx.getSource().sendSuccess(Component.literal(
                    "Truncated-UUID: " + psuData.getUuid().toString().substring(0, 8) + "\nCreated By: " + psuData.meta.getLastAccessedPlayer() + " On: " + sdf.format(new Date(psuData.meta.getFirstAccessedTime())) + "\nLast accessed by: " + psuData.meta.getLastAccessedPlayer() + " on: " + sdf.format(new Date(psuData.meta.getLastAccessedTime()))
                ), false);
            }
        });

        return 0;
    }

    public static int last(CommandContext<CommandSourceStack> ctx, String playerName) {
        StorageManager storageManager = StorageManager.get();

        if (storageManager.getMap().size() == 0) {
            ctx.getSource().sendSuccess(Component.literal("[ ]"), false);
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


        storageManager.getMap().forEach( (uuid, psuData) -> {
            if (psuData.meta.getLastAccessedPlayer().equalsIgnoreCase(playerName)) {
                ctx.getSource().sendSuccess(Component.literal(
                    "Truncated-UUID: " + psuData.getUuid().toString().substring(0, 8) + "\nCreated By: " + psuData.meta.getLastAccessedPlayer() + " On: " + sdf.format(new Date(psuData.meta.getFirstAccessedTime())) + "\nLast accessed by: " + psuData.meta.getLastAccessedPlayer() + " on: " + sdf.format(new Date(psuData.meta.getLastAccessedTime()))
                ), false);
            }
        });

        return 0;
    }
}
