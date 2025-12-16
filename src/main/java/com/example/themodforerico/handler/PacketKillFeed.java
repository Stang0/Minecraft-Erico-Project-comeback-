package com.example.themodforerico.handler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketKillFeed {
    public final String message;
    public final String skullOwner;

    public PacketKillFeed(String message, String skullOwner) {
        this.message = message;
        this.skullOwner = skullOwner;
    }

    public PacketKillFeed(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.skullOwner = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message);
        buf.writeUtf(skullOwner);
    }
}