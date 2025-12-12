package com.example.themodforerico.handler;

import net.minecraft.network.FriendlyByteBuf;

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
}