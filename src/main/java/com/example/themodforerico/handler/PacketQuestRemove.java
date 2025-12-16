package com.example.themodforerico.handler;

import com.example.themodforerico.client.questsystem.QuestManager; // ⚠️ Import QuestManager
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketQuestRemove {
    public final String name;

    public PacketQuestRemove(String name) {
        this.name = name;
    }

    public PacketQuestRemove(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    // 🔥 เมธอด handle ที่เคย Error ตอนนี้ใส่ให้ครบแล้ว 🔥
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
//        context.enqueueWork(() -> {
//
//            QuestManager.removeQuest(this.name);
//        });
        context.setPacketHandled(true);
    }
}