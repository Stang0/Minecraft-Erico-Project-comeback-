package com.example.themodforerico.client;

import com.example.themodforerico.client.killfeed.KillFeedToast;
import com.example.themodforerico.client.questsystem.QuestManager;
import com.example.themodforerico.handler.PacketKillFeed;
import com.example.themodforerico.handler.PacketQuestAdd;
import com.example.themodforerico.handler.PacketQuestRemove;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleKillFeed(PacketKillFeed msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().getToasts().addToast(new KillFeedToast(msg.message, msg.skullOwner));
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handleQuestAdd(PacketQuestAdd msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            QuestManager.addQuest(msg.name, msg.desc, msg.pos, msg.shouldTrack);
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("§6[QUEST] Received: §e" + msg.name));
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handleQuestRemove(PacketQuestRemove msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            QuestManager.removeQuestByName(msg.name);
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("§c[QUEST] Removed: §7" + msg.name));
        });
        ctx.get().setPacketHandled(true);
    }
}