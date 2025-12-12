package com.example.themodforerico.server;

import com.example.themodforerico.TheModForErico;
import com.example.themodforerico.handler.PacketHandler;
import com.example.themodforerico.handler.PacketKillFeed;
import com.example.themodforerico.handler.PacketQuestAdd;
import com.example.themodforerico.handler.PacketQuestRemove;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TheModForErico.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer victim) {
            String skullOwnerName = victim.getName().getString();
            String message = event.getSource().getLocalizedDeathMessage(victim).getString();

            if (victim.getKillCredit() instanceof ServerPlayer killer) {
                message = "§e" + killer.getName().getString() + " §7eliminated §c" + victim.getName().getString();
            }

            // ส่ง Packet หาทุกคน
            PacketHandler.sendToAll(new PacketKillFeed(message, skullOwnerName));
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("quest")
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("track", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "name");
                                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                                                    boolean shouldTrack = BoolArgumentType.getBool(context, "track");
                                                    BlockPos pos = targetPlayer.blockPosition();

                                                    PacketHandler.sendToPlayer(new PacketQuestAdd(name, "Server Quest", pos, shouldTrack), targetPlayer);
                                                    context.getSource().sendSuccess(() -> Component.literal("§a[QUEST] Added '" + name + "' to " + targetPlayer.getName().getString()), true);
                                                    return 1;
                                                })))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("track", BoolArgumentType.bool()) // Argument นี้มีไว้เฉยๆ ตาม request เดิม
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "name");
                                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");

                                                    PacketHandler.sendToPlayer(new PacketQuestRemove(name), targetPlayer);
                                                    context.getSource().sendSuccess(() -> Component.literal("§c[QUEST] Removed '" + name + "' from " + targetPlayer.getName().getString()), true);
                                                    return 1;
                                                })))))
        );
    }
}